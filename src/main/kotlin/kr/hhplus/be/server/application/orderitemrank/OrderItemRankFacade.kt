package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.order.OrderQuery
import kr.hhplus.be.server.domain.order.OrderService
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankCommand
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankQuery
import kr.hhplus.be.server.domain.orderitemrank.OrderItemRankService
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.lock.LockKeyGenerator
import kr.hhplus.be.server.shared.transaction.TransactionHelper
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

/**
 * 주문 아이템 순위 파사드
 * 주문 아이템 순위 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class OrderItemRankFacade(
    private val orderService: OrderService,
    private val orderItemRankService: OrderItemRankService,
    private val transactionHelper: TransactionHelper,
    private val lockManager: DistributedLockManager
) {
    private val logger = LoggerFactory.getLogger(OrderItemRankFacade::class.java)

    /**
     * 최근 N일간의 주문 아이템 중 상위 M개 순위를 조회합니다.
     * Cache-Aside 패턴 사용: 캐시에서 먼저 조회 후 없으면 계산해서 캐시에 저장
     * DistributedLockManager를 사용하여 Cache Stampede 방지
     *
     * @param criteria 순위 조회 기준
     * @return 상위 M개 주문 아이템 순위 목록
     */
    fun getRecentTopOrderItemRanks(criteria: OrderItemRankCriteria.RecentTopRanks = OrderItemRankCriteria.RecentTopRanks()): OrderItemRankResult.List {
        val query = OrderItemRankQuery.GetTopRank(days = criteria.days, limit = criteria.limit)
        
        // 캐시에서 먼저 조회 시도
        val cachedRanks = orderItemRankService.getTopOrderItemRank(query)
        if (cachedRanks != null) {
            // 캐시 히트
            logger.debug("Cache hit for order item ranks: days=${criteria.days}, limit=${criteria.limit}")
            return OrderItemRankResult.List(
                cachedRanks.items.map {
                    OrderItemRankResult.Single.from(productId = it.productId, orderCount = it.orderCount)
                }
            )
        }
        
        // executeWithDomainLock 사용하여 락 획득 및 작업 실행
        try {
            return lockManager.executeWithDomainLock(
                domainPrefix = LockKeyConstants.ORDER_ITEM_RANK_PREFIX,
                resourceType = LockKeyConstants.RESOURCE_ID,
                resourceId = "days_${criteria.days}_limit_${criteria.limit}",
                timeout = LockKeyConstants.DEFAULT_TIMEOUT,
                unit = TimeUnit.SECONDS
            ) {
                // 락 획득 후 다시 캐시 확인 (다른 스레드가 이미 갱신했을 수 있음)
                val doubleCheckRanks = orderItemRankService.getTopOrderItemRank(query)
                if (doubleCheckRanks != null) {
                    logger.debug("Cache was populated by another thread while waiting for lock")
                    return@executeWithDomainLock OrderItemRankResult.List(
                        doubleCheckRanks.items.map {
                            OrderItemRankResult.Single.from(productId = it.productId, orderCount = it.orderCount)
                        }
                    )
                }
                
                // 실제 데이터베이스 조회 및 캐시 갱신
                logger.debug("Refreshing best sellers data: days=${criteria.days}, limit=${criteria.limit}")
                refreshBestSellers(criteria.days, criteria.limit)
                
                // 갱신된 데이터 조회
                val freshRanks = transactionHelper.executeInReadOnlyTransaction {
                    orderItemRankService.getTopOrderItemRank(query)
                }
                
                OrderItemRankResult.List(
                    freshRanks?.items?.map {
                        OrderItemRankResult.Single.from(productId = it.productId, orderCount = it.orderCount)
                    } ?: emptyList()
                )
            }
        } catch (e: Exception) {
            logger.error("Error while acquiring or using lock: ${e.message}", e)
            
            // 락 획득 실패 시 기존 방식으로 데이터 조회 (캐시 없이 직접 DB 조회)
            val fallbackRanks = transactionHelper.executeInReadOnlyTransaction {
                refreshBestSellers(criteria.days, criteria.limit)
                orderItemRankService.getTopOrderItemRank(query)
            }
            
            return OrderItemRankResult.List(
                fallbackRanks?.items?.map {
                    OrderItemRankResult.Single.from(productId = it.productId, orderCount = it.orderCount)
                } ?: emptyList()
            )
        }
    }

    /**
     * 베스트셀러 캐시를 주기적으로 갱신합니다. (Refresh-Ahead 패턴)
     * 10분마다 실행되어 캐시 만료 전에 데이터를 미리 갱신합니다.
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    fun evictBestSellersCache() {
        orderItemRankService.invalidateRankCache()
    }

    fun refreshBestSellers(days: Int, limit: Int) {
        val orderItems = transactionHelper.executeInReadOnlyTransaction {
            orderService.getAggregatedOrderItemsByProductId(OrderQuery.GetAggregatedOrderItems(days = days))
        }

        val rankItems = orderItems.map { (productId, count) ->
            OrderItemRankCommand.SaveTopRank.RankItem(
                productId = productId,
                orderCount = count
            )
        }

        val command = OrderItemRankCommand.SaveTopRank(
            ranks = rankItems,
            days = days,
            limit = limit
        )

        transactionHelper.executeInTransaction {
            orderItemRankService.saveTopOrderItemRank(command)
        }
    }
}
