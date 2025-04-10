package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderDiscountRepository: OrderDiscountRepository
) {
    fun createOrder(command: OrderCommand.Create): Order {
        val orderId = UUID.randomUUID().toString()
        
        // 주문 생성
        val order = Order(
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
            OrderItem(
                orderItemId = UUID.randomUUID().toString(),
                orderId = orderId,
                productId = itemCommand.productId,
                amount = itemCommand.amount,
                unitPrice = itemCommand.unitPrice,
                totalPrice = itemCommand.totalPrice
            )
        }
        orderItemRepository.createAll(orderItems)
        
        // 주문 할인 생성
        if (command.orderDiscounts.isNotEmpty()) {
            val orderDiscounts = command.orderDiscounts.map { discountCommand ->
                OrderDiscount(
                    orderDiscountId = UUID.randomUUID().toString(),
                    orderId = orderId,
                    discountType = discountCommand.discountType,
                    discountId = discountCommand.discountId,
                    discountAmount = discountCommand.discountAmount
                )
            }
            orderDiscountRepository.createAll(orderDiscounts)
        }
        
        return savedOrder
    }
    
    fun getOrderById(command: OrderCommand.GetById): Order {
        return orderRepository.findById(command.orderId)
            ?: throw OrderException.NotFound("Order with id: ${command.orderId}")
    }
    
    fun getOrdersByUserId(command: OrderCommand.GetByUserId): List<Order> {
        return orderRepository.findByUserId(command.userId)
    }
    
    fun getAllOrders(): List<Order> {
        return orderRepository.findAll()
    }
    
    fun getOrderItemsByOrderId(command: OrderItemCommand.GetByOrderId): List<OrderItem> {
        return orderItemRepository.findByOrderId(command.orderId)
    }
    
    fun getOrderDiscountsByOrderId(command: OrderDiscountCommand.GetByOrderId): List<OrderDiscount> {
        return orderDiscountRepository.findByOrderId(command.orderId)
    }
} 