package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderDiscountRepository: OrderDiscountRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun createOrder(command: OrderCommand.Create): Order {
        val orderId = UUID.randomUUID().toString()
        
        // 주문 생성
        val order = Order.create(
            orderId = orderId,
            userId = command.userId,
            paymentId = command.paymentId,
            totalAmount = command.totalAmount,
            totalDiscountAmount = command.totalDiscountAmount,
            finalAmount = command.finalAmount
        )
        val savedOrder = orderRepository.create(order)
        
        // 주문 상품 생성
        val orderItems = command.orderItems.map { itemCommand ->
            OrderItem.create(
                orderItemId = UUID.randomUUID().toString(),
                orderId = orderId,
                productId = itemCommand.productId,
                amount = itemCommand.amount,
                unitPrice = itemCommand.unitPrice
            )
        }
        orderItemRepository.createAll(orderItems)
        
        // 주문 할인 생성
        if (command.orderDiscounts.isNotEmpty()) {
            val orderDiscounts = command.orderDiscounts.map { discountCommand ->
                OrderDiscount.create(
                    orderDiscountId = UUID.randomUUID().toString(),
                    orderId = orderId,
                    orderDiscountType = discountCommand.orderDiscountType,
                    discountId = discountCommand.discountId,
                    discountAmount = discountCommand.discountAmount
                )
            }
            orderDiscountRepository.createAll(orderDiscounts)
        }
        
        // 도메인 이벤트 발행
        eventPublisher.publishEvent(
            OrderDomainEvent.OrderCompleted(
                orderId = savedOrder.orderId,
                userId = savedOrder.userId,
                totalAmount = savedOrder.totalAmount,
                finalAmount = savedOrder.finalAmount,
                paymentId = savedOrder.paymentId
            )
        )
        
        return savedOrder
    }
    
    fun getOrderById(query: OrderQuery.GetById): Order {
        return orderRepository.findById(query.orderId)
            ?: throw OrderException.NotFound("Order with id: ${query.orderId}")
    }
    
    fun getOrdersByUserId(query: OrderQuery.GetByUserId): List<Order> {
        return orderRepository.findByUserId(query.userId)
    }
    
    fun getAllOrders(query: OrderQuery.GetAll): List<Order> {
        return orderRepository.findAll()
    }
    
    fun getOrderItemsByOrderId(query: OrderQuery.GetOrderItemsByOrderId): List<OrderItem> {
        return orderItemRepository.findByOrderId(query.orderId)
    }
    
    fun getOrderDiscountsByOrderId(query: OrderQuery.GetOrderDiscountsByOrderId): List<OrderDiscount> {
        return orderDiscountRepository.findByOrderId(query.orderId)
    }
    
    /**
     * 최근 N일간의 상품별 주문 수량을 집계합니다.
     * 상품ID별로 주문된 총 수량을 계산하여 내림차순으로 정렬합니다.
     * 
     * @param query 집계 조건
     * @return 상품ID와 주문 수량의 Map, 주문 수량 내림차순 정렬
     */
    fun getAggregatedOrderItemsByProductId(query: OrderQuery.GetAggregatedOrderItems): Map<String, Long> {
        // 최근 N일 이내의 주문 조회
        val startDate = LocalDateTime.now().minusDays(query.days.toLong())
        val recentOrders = orderRepository.findByCreatedAtAfter(startDate)
        
        if (recentOrders.isEmpty()) {
            return emptyMap()
        }
        
        // 해당 주문들의 주문 상품 조회
        val orderIds = recentOrders.map { order -> order.orderId }
        val orderItems = orderItemRepository.findByOrderIdIn(orderIds)
        
        // 상품별 주문 수량 집계
        return orderItems.groupBy { orderItem -> orderItem.productId }
            .mapValues { (_, items) -> items.sumOf { orderItem -> orderItem.amount } }
            .toList()
            .sortedByDescending { (_, count) -> count }
            .take(query.limit)
            .toMap()
    }
} 