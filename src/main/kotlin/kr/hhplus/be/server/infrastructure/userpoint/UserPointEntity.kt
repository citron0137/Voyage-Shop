package kr.hhplus.be.server.infrastructure.userpoint

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.userpoint.UserPoint
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * UserPoint 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "user_points")
data class UserPointEntity(
    @Id
    val userPointId: String,
    
    val userId: String,
    
    var amount: Long,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    var updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun from(userPoint: UserPoint): UserPointEntity {
            return UserPointEntity(
                userPointId = userPoint.userPointId,
                userId = userPoint.userId,
                amount = userPoint.amount,
                createdAt = userPoint.createdAt,
                updatedAt = userPoint.updatedAt
            )
        }
    }
    
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toUserPoint(): UserPoint {
        return UserPoint(
            userPointId = userPointId,
            userId = userId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 