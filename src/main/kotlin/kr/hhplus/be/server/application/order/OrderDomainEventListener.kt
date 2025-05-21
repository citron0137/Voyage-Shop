package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.order.OrderDomainEvent
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.stereotype.Component
import org.springframework.context.ApplicationEventPublisher

@Component
class OrderDomainEventListener(
    private val eventPublisher: ApplicationEventPublisher,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCompleted(event: OrderDomainEvent.OrderCompleted) {
        // 도메인 이벤트를 애플리케이션 이벤트로 변환하여 발행
        // 필요시 추가 데이터를 조회하여 조합할 수 있음
        val applicationEvent = OrderApplicationEvent.OrderCompleted(
            orderId = event.orderId,
            userId = event.userId,
            totalAmount = event.totalAmount,
            finalAmount = event.finalAmount,
            paymentId = event.paymentId
        )
        eventPublisher.publishEvent(applicationEvent)
    }
} 