package kr.hhplus.be.server.domain.couponevent

import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEventException
import java.time.LocalDateTime
import java.util.UUID

sealed class CouponEventCommand {
    
    data class Create(
        val benefitMethod: CouponEventBenefitMethod,
        val benefitAmount: String,
        val totalIssueAmount: Long,
    ) : CouponEventCommand() {
        init {
            if (benefitAmount.isBlank()) {
                throw CouponEventException.InvalidBenefitAmount("Benefit amount should not be blank")
            }
            
            if (totalIssueAmount <= 0) {
                throw CouponEventException.InvalidIssueAmount("Total issue amount should be greater than 0")
            }
        }
        
        fun toEntity(): CouponEvent {
            return CouponEvent(
                id = UUID.randomUUID().toString(),
                benefitMethod = benefitMethod,
                benefitAmount = benefitAmount,
                totalIssueAmount = totalIssueAmount,
                leftIssueAmount = totalIssueAmount,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
    
    data class GetById(
        val id: String
    ) : CouponEventCommand() {
        init {
            if (id.isBlank()) {
                throw CouponEventException.InvalidId("Coupon event ID should not be blank")
            }
        }
    }
    
    class GetAll : CouponEventCommand()
    
    data class Issue(
        val id: String
    ) : CouponEventCommand() {
        init {
            if (id.isBlank()) {
                throw CouponEventException.InvalidId("Coupon event ID should not be blank")
            }
        }
    }
} 