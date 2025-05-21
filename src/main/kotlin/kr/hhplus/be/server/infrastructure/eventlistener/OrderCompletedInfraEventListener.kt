package kr.hhplus.be.server.application.order

import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.slf4j.LoggerFactory

@Component
class OrderCompletedInfraEventListener(
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Async
    @EventListener
    fun callExternalApiWhenOrderCompleted(event: OrderApplicationEvent.OrderCompleted) {
        logger.info("Order completed event received: {}", event)
        logger.info("Send order completed event to external API: {}", event)
    }
} 