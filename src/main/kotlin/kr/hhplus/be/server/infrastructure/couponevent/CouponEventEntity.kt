package kr.hhplus.be.server.infrastructure.couponevent

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.couponevent.BenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEvent
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 쿠폰 이벤트 엔티티
 */
@Entity
@Table(name = "coupon_event")
class CouponEventEntity(
    @Id
    @Column(name = "id", length = 36, nullable = false)
    val id: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "benefit_method", nullable = false)
    val benefitMethod: BenefitMethod,

    @Column(name = "benefit_amount", nullable = false)
    val benefitAmount: String,

    @Column(name = "total_issue_amount", nullable = false)
    val totalIssueAmount: Long,

    @Column(name = "left_issue_amount", nullable = false)
    var leftIssueAmount: Long,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {

    companion object {
        /**
         * 도메인 객체를 엔티티로 변환
         */
        fun of(couponEvent: CouponEvent): CouponEventEntity {
            return CouponEventEntity(
                id = couponEvent.id,
                benefitMethod = couponEvent.benefitMethod,
                benefitAmount = couponEvent.benefitAmount,
                totalIssueAmount = couponEvent.totalIssueAmount,
                leftIssueAmount = couponEvent.leftIssueAmount,
                createdAt = couponEvent.createdAt,
                updatedAt = couponEvent.updatedAt
            )
        }

        /**
         * 엔티티를 도메인 객체로 변환
         */
        fun toDomain(entity: CouponEventEntity): CouponEvent {
            return CouponEvent(
                id = entity.id,
                benefitMethod = entity.benefitMethod,
                benefitAmount = entity.benefitAmount,
                totalIssueAmount = entity.totalIssueAmount,
                leftIssueAmount = entity.leftIssueAmount,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
} 