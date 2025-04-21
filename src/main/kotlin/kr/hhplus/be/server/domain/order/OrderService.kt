package kr.hhplus.be.server.domain.order

import org.springframework.stereotype.Service
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
} 