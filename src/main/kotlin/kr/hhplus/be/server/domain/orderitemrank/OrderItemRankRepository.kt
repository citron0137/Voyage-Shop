package kr.hhplus.be.server.domain.orderitemrank


/**
 * 주문 상품 순위 리포지터리 인터페이스
 * 상품별 주문량에 따른 인기 순위를 관리합니다.
 */
interface OrderItemRankRepository {
    /**
     * 상위 랭킹 상품들을 저장합니다.
     *
     * @param orderItemRank 저장할 상품 랭킹 컬렉션
     * @return 저장된 상품 랭킹 컬렉션
     */
    fun saveRank(orderItemRank: OrderItemRank): OrderItemRank
    
    /**
     * 상위 랭킹 상품들을 조회합니다.
     *
     * @param days 최근 몇일 간의 데이터를 조회할지
     * @param limit 몇 개의 상품을 조회할지
     * @return 상위 랭킹 상품 컬렉션, 캐시에 없으면 빈 컬렉션 반환
     */
    fun getRank(days: Int, limit: Int): OrderItemRank?
    
    /**
     * 저장된 모든 랭킹 데이터를 무효화합니다.
     */
    fun invalidateCache()
} 