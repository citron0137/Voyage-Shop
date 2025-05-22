package kr.hhplus.be.server.infrastructure.couponevent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

data class CouponEventRedisEntity(
    val id: String,
    val benefitMethod: CouponEventBenefitMethod,
    val benefitAmount: String,
    val totalIssueAmount: Long,
    var leftIssueAmount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    /**
     * 엔티티를 도메인 객체로 변환
     */
    fun toDomain(): CouponEvent {
        return CouponEvent(
            id = id,
            benefitMethod = benefitMethod,
            benefitAmount = benefitAmount,
            totalIssueAmount = totalIssueAmount,
            leftIssueAmount = leftIssueAmount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        /**
         * 도메인 객체를 엔티티로 변환
         */
        fun fromDomain(domain: CouponEvent): CouponEventRedisEntity {
            return CouponEventRedisEntity(
                id = domain.id,
                benefitMethod = domain.benefitMethod,
                benefitAmount = domain.benefitAmount,
                totalIssueAmount = domain.totalIssueAmount,
                leftIssueAmount = domain.leftIssueAmount,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
}