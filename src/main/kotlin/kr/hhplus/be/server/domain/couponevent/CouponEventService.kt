package kr.hhplus.be.server.domain.couponevent

import kr.hhplus.be.server.domain.couponevent.CouponEventBenefitMethod
import kr.hhplus.be.server.domain.couponevent.CouponEventCommand
import kr.hhplus.be.server.domain.couponevent.CouponEventException
import kr.hhplus.be.server.domain.couponevent.CouponEventQuery
import kr.hhplus.be.server.domain.couponevent.CouponEventRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponEventService(
    private val repository: CouponEventRepository
) {
    /**
     * 쿠폰 이벤트를 생성합니다.
     *
     * @param command 쿠폰 이벤트 생성 요청 커맨드
     * @return 생성된 쿠폰 이벤트
     */
    fun createCouponEvent(command: CouponEventCommand.Create): CouponEvent {
        return repository.create(command.toEntity())
    }

    /**
     * ID로 쿠폰 이벤트를 조회합니다.
     *
     * @param query 쿠폰 이벤트 조회 쿼리
     * @return 조회된 쿠폰 이벤트
     * @throws CouponEventException.NotFound 쿠폰 이벤트를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getCouponEvent(query: CouponEventQuery.GetById): CouponEvent {
        return repository.findById(query.id)
            ?: throw CouponEventException.NotFound(query.id)
    }

    /**
     * 모든 쿠폰 이벤트를 조회합니다.
     *
     * @param query 쿠폰 이벤트 전체 조회 쿼리
     * @return 쿠폰 이벤트 목록
     */
    @Transactional(readOnly = true)
    fun getAllCouponEvents(query: CouponEventQuery.GetAll): List<CouponEvent> {
        return repository.findAll()
    }

    /**
     * 쿠폰 이벤트의 재고를 감소시킵니다.
     * 비관적 락을 사용하여 동시성 문제를 방지합니다.
     *
     * @param command 재고 감소 명령
     * @return 업데이트된 쿠폰 이벤트
     * @throws CouponEventException.NotFound 쿠폰 이벤트를 찾을 수 없는 경우
     * @throws CouponEventException.OutOfStock 재고가 없는 경우
     */
    @Transactional
    fun decreaseStock(command: CouponEventCommand.Issue): CouponEvent {
        val couponEvent = repository.findById(command.id)
            ?: throw CouponEventException.NotFound(command.id)
        // 엔티티 내부에서 검증 후 새 객체 생성
        val updatedCouponEvent = couponEvent.decreaseLeftIssueAmount()
        // 리포지토리를 통해 업데이트
        return repository.save(updatedCouponEvent)
    }

    @Transactional
    fun updateRdb(){ repository.updateRdb() }
} 