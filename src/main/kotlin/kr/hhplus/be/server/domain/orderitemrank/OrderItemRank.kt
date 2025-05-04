package kr.hhplus.be.server.domain.orderitemrank

import java.time.LocalDateTime

/**
 * 주문 상품 순위 도메인 모델
 * 상품별 주문량에 따른 인기 순위와 그 컬렉션을 표현합니다.
 */
data class OrderItemRank(
    val items: List<Item>,
    val periodInDays: Int,
    val limit: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 컬렉션이 비어있는지 확인합니다.
     */
    fun isEmpty(): Boolean = items.isEmpty()
    
    /**
     * 컬렉션의 아이템 개수를 반환합니다.
     */
    fun size(): Int = items.size
    
    /**
     * 특정 순위의 아이템을 반환합니다.
     */
    fun getItemByRank(rank: Int): Item? {
        return items.find { it.rank == rank }
    }
    
    /**
     * 특정 상품의 순위 정보를 반환합니다.
     */
    fun getItemByProductId(productId: String): Item? {
        return items.find { it.productId == productId }
    }
    
    /**
     * 주문 상품 순위 개별 아이템
     * 상품별 주문량에 따른 인기 순위를 표현합니다.
     */
    data class Item(
        val productId: String,
        val orderCount: Long,
        val rank: Int = 0,
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val updatedAt: LocalDateTime = LocalDateTime.now()
    ) {
        companion object {
            /**
             * 새 Item 객체를 생성합니다.
             */
            fun create(productId: String, orderCount: Long, rank: Int = 0): Item {
                return Item(
                    productId = productId,
                    orderCount = orderCount,
                    rank = rank
                )
            }
        }
    }
    
    companion object {
        /**
         * 새 OrderItemRanks 객체를 생성합니다.
         */
        fun create(items: List<Item>, days: Int, limit: Int): OrderItemRank {
            // 순위가 정해지지 않은 경우 순위를 매깁니다
            val rankedItems = if (items.all { it.rank == 0 }) {
                items.mapIndexed { index, item -> 
                    item.copy(rank = index + 1)
                }
            } else {
                items
            }
            
            return OrderItemRank(
                items = rankedItems,
                periodInDays = days,
                limit = limit
            )
        }
        
        /**
         * 빈 OrderItemRanks 객체를 생성합니다.
         */
        fun empty(days: Int, limit: Int): OrderItemRank {
            return OrderItemRank(
                items = emptyList(),
                periodInDays = days,
                limit = limit
            )
        }
    }
} 