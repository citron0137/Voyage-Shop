package kr.hhplus.be.server.infrastructure.coupon

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * CouponUser 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "coupon_users")
data class CouponUserJpaEntity(
    @Id
    val couponUserId: String,

    val userId: String,

    @Enumerated(EnumType.STRING)
    val benefitMethod: CouponUserBenefitMethod,

    val benefitAmount: String,

    val usedAt: LocalDateTime?,

    @CreationTimestamp
    val createdAt: LocalDateTime,

    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): CouponUser {
        return CouponUser(
            couponUserId = couponUserId,
            userId = userId,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            usedAt = usedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: CouponUser): CouponUserJpaEntity {
            return CouponUserJpaEntity(
                couponUserId = domain.couponUserId,
                userId = domain.userId,
                benefitMethod = domain.benefitMethod,
                benefitAmount = domain.benefitAmount,
                usedAt = domain.usedAt,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 