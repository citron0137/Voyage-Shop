package kr.hhplus.be.server.domain.orderitemrank

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 상품 순위 서비스
 * 상품별 주문량 순위에 관한 비즈니스 로직을 제공합니다.
 */
@Service
class OrderItemRankService(
    private val orderItemRankRepository: OrderItemRankRepository
) {
    /**
     * 상위 랭킹 상품들을 조회합니다.
     * 캐시에서 조회합니다. 캐시에 없는 경우 빈 컬렉션을 반환합니다.
     *
     * @param query 순위 조회 쿼리
     * @return 상위 랭킹 상품 컬렉션
     */
    @Transactional(readOnly = true)
    fun getTopOrderItemRank(query: OrderItemRankQuery.GetTopRank): OrderItemRank? {
        return orderItemRankRepository.getRank(query.days, query.limit)
    }
    
    /**
     * 상위 랭킹 상품들을 저장합니다.
     *
     * @param command 저장할 랭킹 상품 정보를 담은 명령 객체
     * @return 저장된 랭킹 상품 컬렉션
     */
    @Transactional
    fun saveTopOrderItemRank(command: OrderItemRankCommand.SaveTopRank): OrderItemRank {
        return orderItemRankRepository.saveRank(command.toDomain())
    }
    
    /**
     * 캐시된 베스트셀러 데이터를 무효화합니다.
     * 주문 패턴이 변경되어 캐시 갱신이 필요할 때 호출합니다.
     */
    @Transactional
    fun invalidateRankCache() {
        orderItemRankRepository.invalidateCache()
    }
} 