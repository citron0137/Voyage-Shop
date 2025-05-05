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
    fun getRecentTopOrderItemRanks(criteria: OrderItemRankCriteria.RecentTopRanks = OrderItemRankCriteria.RecentTopRanks()): OrderItemRankResult.Rank {

        val query = OrderItemRankQuery.GetTopRank(days = criteria.days, limit = criteria.limit)
        
        // 캐시에서 먼저 조회 시도
        val cachedRanks = orderItemRankService.getTopOrderItemRank(query)
        if (cachedRanks != null) {
            logger.info("Cache hit for order item ranks: days=${criteria.days}, limit=${criteria.limit}")
            return OrderItemRankResult.Rank.from(cachedRanks)
        }
        
        // executeWithDomainLock 사용하여 락 획득 및 작업 실행
        try {
            lockManager.executeWithDomainLock(
                domainPrefix = LockKeyConstants.ORDER_ITEM_RANK_PREFIX,
                resourceType = LockKeyConstants.RESOURCE_ID,
                resourceId = "days_${criteria.days}_limit_${criteria.limit}",
                timeout = LockKeyConstants.DEFAULT_TIMEOUT,
                unit = TimeUnit.SECONDS
            ) {
                // 락 획득 후 다시 캐시 확인 (다른 스레드가 이미 갱신했을 수 있음)
                val doubleCheckRanks = orderItemRankService.getTopOrderItemRank(query)
                if (doubleCheckRanks != null) {
                    logger.info("Cache was populated by another thread while waiting for lock")
                    return@executeWithDomainLock OrderItemRankResult.Rank.from(doubleCheckRanks)
                }
                return@executeWithDomainLock refreshBestSellers(3, 5)
            }
        } catch (e: Exception) {
            logger.error("Error while acquiring or using lock: ${e.message}", e)
        }
        return refreshBestSellers(3,5)
    }

    /**
     * 베스트셀러 캐시를 주기적으로 갱신합니다. (Refresh-Ahead 패턴)
     * 10분마다 실행되어 캐시 만료 전에 데이터를 미리 갱신합니다.
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    fun evictBestSellersCache() {
        orderItemRankService.invalidateRankCache()
    }

    // refreshBestSellers 메서드는 더 이상 사용하지 않으므로 제거하거나 
    // 아래와 같이 단일 트랜잭션으로 수정
    private fun refreshBestSellers(days: Int, limit: Int): OrderItemRankResult.Rank {
        logger.info("Refreshing best sellers data: days=${days}, limit=${limit}")
        return transactionHelper.executeInTransaction {
            // 주문 데이터 조회
            val orderItems = orderService.getAggregatedOrderItemsByProductId(
                OrderQuery.GetAggregatedOrderItems(days = days)
            )
            logger.info("조회된 주문 아이템 수: ${orderItems.size}")
            // 랭킹 아이템 생성
            val rankItems = orderItems.map { (productId, count) ->
                OrderItemRankCommand.SaveTopRank.RankItem(
                    productId = productId,
                    orderCount = count
                )
            }
            logger.info("저장할 랭킹 아이템 수: ${rankItems.size}")
            // 캐시 저장
            val command = OrderItemRankCommand.SaveTopRank(
                ranks = rankItems,
                days = days,
                limit = limit
            )
            // 저장 후 바로 결과 반환
            val savedRanks = orderItemRankService.saveTopOrderItemRank(command)
            logger.info("캐시 저장 결과: ${savedRanks.items.size ?: 0}개 아이템")
            OrderItemRankResult.Rank.from(savedRanks)
        }
    }

    /**
     * 주문 아이템 순위 데이터를 초기화합니다.
     * 모든 캐시된 랭킹 정보를 삭제합니다.
     */
    fun resetOrderItemRanks() {
        try {
            // 분산 락을 사용하여 여러 인스턴스에서 동시에 초기화하는 것을 방지
            lockManager.executeWithDomainLock(
                domainPrefix = LockKeyConstants.ORDER_ITEM_RANK_PREFIX,
                resourceType = LockKeyConstants.RESOURCE_ID,
                resourceId = "reset_all",
                timeout = LockKeyConstants.DEFAULT_TIMEOUT,
                unit = TimeUnit.SECONDS
            ) {
                logger.info("주문 아이템 순위 데이터를 초기화합니다.")
                orderItemRankService.invalidateRankCache()
                logger.info("주문 아이템 순위 데이터 초기화 완료")
            }
        } catch (e: Exception) {
            logger.error("주문 아이템 순위 데이터 초기화 중 오류 발생: ${e.message}", e)
            // 락 획득 실패시 그냥 직접 수행
            orderItemRankService.invalidateRankCache()
        }
    }
}
