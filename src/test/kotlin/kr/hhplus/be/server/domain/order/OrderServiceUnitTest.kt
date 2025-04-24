package kr.hhplus.be.server.domain.order

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class OrderServiceUnitTest {
    private lateinit var orderRepository: OrderRepository
    private lateinit var orderItemRepository: OrderItemRepository
    private lateinit var orderDiscountRepository: OrderDiscountRepository
    private lateinit var orderService: OrderService

    @BeforeEach
    fun setUp() {
        orderRepository = mockk()
        orderItemRepository = mockk()
        orderDiscountRepository = mockk()
        orderService = OrderService(orderRepository, orderItemRepository, orderDiscountRepository)
    }

    @Test
    fun `주문을 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val paymentId = "test-payment-id"
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            ),
            OrderItemCommand.Create(
                productId = "product-2",
                amount = 1,
                unitPrice = 3000
            )
        )
        val command = OrderCommand.Create(userId, paymentId, orderItems)
        
        val expectedOrder = Order(
            orderId = "test-order-id",
            userId = userId,
            paymentId = paymentId,
            totalAmount = 5000,
            totalDiscountAmount = 0,
            finalAmount = 5000
        )
        
        every { orderRepository.create(any()) } returns expectedOrder
        every { orderItemRepository.createAll(any()) } returns listOf()
        
        // when
        val result = orderService.createOrder(command)

        // then
        verify { orderRepository.create(any()) }
        verify { orderItemRepository.createAll(any()) }
        assertEquals(expectedOrder.orderId, result.orderId)
        assertEquals(expectedOrder.userId, result.userId)
        assertEquals(expectedOrder.paymentId, result.paymentId)
        assertEquals(expectedOrder.totalAmount, result.totalAmount)
        assertEquals(expectedOrder.totalDiscountAmount, result.totalDiscountAmount)
        assertEquals(expectedOrder.finalAmount, result.finalAmount)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `할인이 적용된 주문을 생성할 수 있다`() {
        // given
        val userId = "test-user-id"
        val paymentId = "test-payment-id"
        val orderItems = listOf(
            OrderItemCommand.Create(
                productId = "product-1",
                amount = 2,
                unitPrice = 1000
            ),
            OrderItemCommand.Create(
                productId = "product-2",
                amount = 1,
                unitPrice = 3000
            )
        )
        val orderDiscounts = listOf(
            OrderDiscountCommand.Create(
                orderDiscountType = OrderDiscountType.COUPON,
                discountId = "coupon-1",
                discountAmount = 500
            )
        )
        val command = OrderCommand.Create(userId, paymentId, orderItems, orderDiscounts)
        
        val expectedOrder = Order(
            orderId = "test-order-id",
            userId = userId,
            paymentId = paymentId,
            totalAmount = 5000,
            totalDiscountAmount = 500,
            finalAmount = 4500
        )
        
        every { orderRepository.create(any()) } returns expectedOrder
        every { orderItemRepository.createAll(any()) } returns listOf()
        every { orderDiscountRepository.createAll(any()) } returns listOf()
        
        // when
        val result = orderService.createOrder(command)

        // then
        verify { orderRepository.create(any()) }
        verify { orderItemRepository.createAll(any()) }
        verify { orderDiscountRepository.createAll(any()) }
        assertEquals(expectedOrder.orderId, result.orderId)
        assertEquals(expectedOrder.userId, result.userId)
        assertEquals(expectedOrder.paymentId, result.paymentId)
        assertEquals(expectedOrder.totalAmount, result.totalAmount)
        assertEquals(expectedOrder.totalDiscountAmount, result.totalDiscountAmount)
        assertEquals(expectedOrder.finalAmount, result.finalAmount)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `ID로 주문을 조회할 수 있다`() {
        // given
        val orderId = UUID.randomUUID().toString()
        val command = OrderQuery.GetById(orderId)
        
        val expectedOrder = Order(
            orderId = orderId,
            userId = "test-user-id",
            paymentId = "test-payment-id",
            totalAmount = 5000,
            totalDiscountAmount = 0,
            finalAmount = 5000
        )
        
        every { orderRepository.findById(orderId) } returns expectedOrder

        // when
        val result = orderService.getOrderById(command)

        // then
        verify { orderRepository.findById(orderId) }
        assertEquals(expectedOrder, result)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 예외가 발생한다`() {
        // given
        val orderId = UUID.randomUUID().toString()
        val command = OrderQuery.GetById(orderId)
        
        every { orderRepository.findById(orderId) } returns null

        // when & then
        assertThrows<OrderException.NotFound> {
            orderService.getOrderById(command)
        }
    }

    @Test
    fun `사용자 ID로 주문 목록을 조회할 수 있다`() {
        // given
        val userId = "test-user-id"
        val command = OrderQuery.GetByUserId(userId)
        
        val expectedOrders = listOf(
            Order(
                orderId = UUID.randomUUID().toString(),
                userId = userId,
                paymentId = "payment-1",
                totalAmount = 5000,
                totalDiscountAmount = 0,
                finalAmount = 5000
            ),
            Order(
                orderId = UUID.randomUUID().toString(),
                userId = userId,
                paymentId = "payment-2",
                totalAmount = 8000,
                totalDiscountAmount = 500,
                finalAmount = 7500
            )
        )
        
        every { orderRepository.findByUserId(userId) } returns expectedOrders

        // when
        val results = orderService.getOrdersByUserId(command)

        // then
        verify { orderRepository.findByUserId(userId) }
        assertEquals(expectedOrders.size, results.size)
        results.forEachIndexed { index, order ->
            assertEquals(expectedOrders[index], order)
            assertNotNull(order.createdAt)
            assertNotNull(order.updatedAt)
        }
    }

    @Test
    fun `모든 주문을 조회할 수 있다`() {
        // given
        val expectedOrders = listOf(
            Order(
                orderId = UUID.randomUUID().toString(),
                userId = "user-1",
                paymentId = "payment-1",
                totalAmount = 5000,
                totalDiscountAmount = 0,
                finalAmount = 5000
            ),
            Order(
                orderId = UUID.randomUUID().toString(),
                userId = "user-2",
                paymentId = "payment-2",
                totalAmount = 8000,
                totalDiscountAmount = 500,
                finalAmount = 7500
            )
        )
        
        every { orderRepository.findAll() } returns expectedOrders

        // when
        val results = orderService.getAllOrders(OrderQuery.GetAll)

        // then
        verify { orderRepository.findAll() }
        assertEquals(expectedOrders, results)
        results.forEach {
            assertNotNull(it.createdAt)
            assertNotNull(it.updatedAt)
        }
    }
    
    @Test
    fun `주문 ID로 주문 상품 목록을 조회할 수 있다`() {
        // given
        val orderId = "test-order-id"
        val command = OrderQuery.GetOrderItemsByOrderId(orderId)
        
        val expectedOrderItems = listOf(
            OrderItem(
                orderItemId = UUID.randomUUID().toString(),
                orderId = orderId,
                productId = "product-1",
                amount = 2,
                unitPrice = 1000,
                totalPrice = 2000
            ),
            OrderItem(
                orderItemId = UUID.randomUUID().toString(),
                orderId = orderId,
                productId = "product-2",
                amount = 1,
                unitPrice = 3000,
                totalPrice = 3000
            )
        )
        
        every { orderItemRepository.findByOrderId(orderId) } returns expectedOrderItems

        // when
        val results = orderService.getOrderItemsByOrderId(command)

        // then
        verify { orderItemRepository.findByOrderId(orderId) }
        assertEquals(expectedOrderItems.size, results.size)
        results.forEachIndexed { index, orderItem ->
            assertEquals(expectedOrderItems[index], orderItem)
            assertNotNull(orderItem.createdAt)
            assertNotNull(orderItem.updatedAt)
        }
    }
    
    @Test
    fun `주문 ID로 주문 할인 목록을 조회할 수 있다`() {
        // given
        val orderId = "test-order-id"
        val command = OrderQuery.GetOrderDiscountsByOrderId(orderId)
        
        val expectedOrderDiscounts = listOf(
            OrderDiscount(
                orderDiscountId = UUID.randomUUID().toString(),
                orderId = orderId,
                type = OrderDiscountType.COUPON,
                discountId = "coupon-1",
                discountAmount = 500
            )
        )
        
        every { orderDiscountRepository.findByOrderId(orderId) } returns expectedOrderDiscounts

        // when
        val results = orderService.getOrderDiscountsByOrderId(command)

        // then
        verify { orderDiscountRepository.findByOrderId(orderId) }
        assertEquals(expectedOrderDiscounts.size, results.size)
        results.forEachIndexed { index, orderDiscount ->
            assertEquals(expectedOrderDiscounts[index], orderDiscount)
            assertNotNull(orderDiscount.createdAt)
            assertNotNull(orderDiscount.updatedAt)
        }
    }
} 