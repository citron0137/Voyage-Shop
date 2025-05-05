package kr.hhplus.be.server.infrastructure.orderitemrank

import java.io.Serializable
import java.time.LocalDateTime

/**
 * Redis에 저장될 상품 랭킹 엔티티
 * 도메인 모델 OrderItemRank와 유사한 구조를 가지며 직렬화가 가능하도록 Serializable을 구현합니다.
 */
data class OrderItemRankRedisEntity(
    val items: List<Item>,
    val periodInDays: Int,
    val limit: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) : Serializable {
    
    /**
     * Redis에 저장될 상품 랭킹 아이템 엔티티
     */
    data class Item(
        val productId: String,
        val orderCount: Long,
        val rank: Int,
        val createdAt: LocalDateTime = LocalDateTime.now(),
        val updatedAt: LocalDateTime = LocalDateTime.now()
    ) : Serializable
    
    companion object {
        /**
         * 도메인 모델 OrderItemRank를 Redis 엔티티로 변환합니다.
         */
        fun fromDomain(domain: kr.hhplus.be.server.domain.orderitemrank.OrderItemRank): OrderItemRankRedisEntity {
            return OrderItemRankRedisEntity(
                items = domain.items.map { 
                    Item(
                        productId = it.productId,
                        orderCount = it.orderCount,
                        rank = it.rank,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    ) 
                },
                periodInDays = domain.periodInDays,
                limit = domain.limit,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
        
        /**
         * Redis 엔티티를 도메인 모델 OrderItemRank로 변환합니다.
         */
        fun toDomain(entity: OrderItemRankRedisEntity): kr.hhplus.be.server.domain.orderitemrank.OrderItemRank {
            return kr.hhplus.be.server.domain.orderitemrank.OrderItemRank(
                items = entity.items.map { 
                    kr.hhplus.be.server.domain.orderitemrank.OrderItemRank.Item(
                        productId = it.productId,
                        orderCount = it.orderCount,
                        rank = it.rank,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    ) 
                },
                periodInDays = entity.periodInDays,
                limit = entity.limit,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
} 