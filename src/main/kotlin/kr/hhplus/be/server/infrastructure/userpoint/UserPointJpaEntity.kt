package kr.hhplus.be.server.infrastructure.userpoint

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.userpoint.UserPoint
import java.time.LocalDateTime
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp

/**
 * 사용자 포인트 JPA 엔티티 클래스
 */
@Entity
@Table(name = "user_points")
class UserPointJpaEntity(
    /**
     * 사용자 포인트 ID
     */
    @Id
    @Column(name = "user_point_id")
    val userPointId: String,

    /**
     * 사용자 ID
     */
    @Column(name = "user_id")
    val userId: String,

    /**
     * 포인트 금액
     */
    @Column(name = "amount")
    val amount: Long,

    /**
     * 생성 일시
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    /**
     * 수정 일시
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 도메인 객체로부터 JPA 엔티티 생성
         */
        fun fromDomain(domain: UserPoint): UserPointJpaEntity {
            return UserPointJpaEntity(
                userPointId = domain.userPointId,
                userId = domain.userId,
                amount = domain.amount,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }

    /**
     * JPA 엔티티를 도메인 객체로 변환
     */
    fun toDomain(): UserPoint {
        return UserPoint(
            userPointId = userPointId,
            userId = userId,
            amount = amount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
} 