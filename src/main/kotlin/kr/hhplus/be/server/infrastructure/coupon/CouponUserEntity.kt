package kr.hhplus.be.server.infrastructure.coupon

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.coupon.CouponBenefitMethod
import kr.hhplus.be.server.domain.coupon.CouponUser
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * CouponUser 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "coupon_users")
data class CouponUserEntity(
    @Id
    val couponUserId: String,
    
    val userId: String,
    
    val benefitMethod: CouponBenefitMethod,
    
    val benefitAmount: String,
    
    val usedAt: LocalDateTime?,
    
    @CreationTimestamp
    val createdAt: LocalDateTime,
    
    @UpdateTimestamp
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun from(couponUser: CouponUser): CouponUserEntity {
            return CouponUserEntity(
                couponUserId = couponUser.couponUserId,
                userId = couponUser.userId,
                benefitMethod = couponUser.benefitMethod,
                benefitAmount = couponUser.benefitAmount,
                usedAt = couponUser.usedAt,
                createdAt = couponUser.createdAt,
                updatedAt = couponUser.updatedAt
            )
        }
    }
    
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toCouponUser(): CouponUser {
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
} 