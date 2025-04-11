package kr.hhplus.be.server.domain.couponevent

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventService(
    private val couponEventRepository: CouponEventRepository
) {
    @Transactional
    fun createCouponEvent(command: CreateCouponEventCommand): CouponEvent {
        val couponEvent = command.toCouponEvent()
        return couponEventRepository.save(couponEvent)
    }
    
    fun getCouponEvent(id: String): CouponEvent {
        return couponEventRepository.findById(id)
            .orElseThrow { CENotFoundException(id) }
    }
    
    fun getAllCouponEvents(): List<CouponEvent> {
        return couponEventRepository.findAll()
    }
    
    /**
     * 쿠폰 이벤트의 재고를 감소시킵니다.
     * 
     * @param id 쿠폰 이벤트 ID
     * @return 재고 감소 후 업데이트된 쿠폰 이벤트 엔티티
     * @throws CEStockEmptyException 재고가 없는 경우 발생
     */
    @Transactional
    fun decreaseStock(id: String): CouponEvent {
        // 현재 쿠폰 이벤트 조회
        val couponEvent = getCouponEvent(id)
        
        // 재고 확인
        if (couponEvent.leftIssueAmount <= 0) {
            throw CEStockEmptyException(id)
        }
        
        // 도메인 객체에서 재고 감소
        couponEvent.decreaseLeftIssueAmount()
        
        // 변경된 엔티티 저장
        return couponEventRepository.save(couponEvent)
    }
    
    fun canIssue(id: String): Boolean {
        val couponEvent = getCouponEvent(id)
        return couponEvent.leftIssueAmount > 0
    }
} 