package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.couponuser.CouponUser
import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.payment.Payment
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.Product
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductQuery
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.User
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@DisplayName("OrderFacade 테스트")
class OrderFacadeTest {
    
    private lateinit var orderService: OrderService
    private lateinit var userService: UserService
    private lateinit var productService: ProductService
    private lateinit var paymentService: PaymentService
    private lateinit var couponUserService: CouponUserService
    private lateinit var orderFacade: OrderFacade
    
    @BeforeEach
    fun setup() {
        orderService = mock()
        userService = mock()
        productService = mock()
        paymentService = mock()
        couponUserService = mock()
        orderFacade = OrderFacade(
            orderService = orderService,
            userService = userService,
            productService = productService,
            paymentService = paymentService,
            couponUserService = couponUserService
        )
    }
    
    @Test
    @DisplayName("주문 ID로 주문을 조회한다")
    fun getOrder() {
        // given
        val orderId = "order1"
        val userId = "user1"
        val order = Order(
            orderId = orderId,
            userId = userId,
            paymentId = "payment1",
            totalAmount = 10000,
            totalDiscountAmount = 1000,
            finalAmount = 9000,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val orderItems = listOf(
            OrderItem(
                orderItemId = "orderItem1",
                orderId = orderId,
                productId = "product1",
                amount = 2,
                unitPrice = 5000,
                totalPrice = 10000
            )
        )
        
        val orderDiscounts = listOf(
            OrderDiscount(
                orderDiscountId = "orderDiscount1",
                orderId = orderId,
                discountType = DiscountType.COUPON,
                discountId = "coupon1",
                discountAmount = 1000
            )
        )
        
        whenever(orderService.getOrderById(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(any())).thenReturn(orderItems)
        whenever(orderService.getOrderDiscountsByOrderId(any())).thenReturn(orderDiscounts)
        
        // when
        val result = orderFacade.getOrder(orderId)
        
        // then
        assertThat(result.orderId).isEqualTo(orderId)
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.totalAmount).isEqualTo(10000)
        assertThat(result.totalDiscountAmount).isEqualTo(1000)
        assertThat(result.finalAmount).isEqualTo(9000)
        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].productId).isEqualTo("product1")
        assertThat(result.discounts).hasSize(1)
        assertThat(result.discounts[0].discountType).isEqualTo(DiscountType.COUPON)
    }
    
    @Test
    @DisplayName("빈 주문 ID로 조회하면 예외가 발생한다")
    fun getOrderWithBlankId() {
        // given
        val orderId = ""
        
        // when, then
        assertThrows<OrderException.OrderIdShouldNotBlank> {
            orderFacade.getOrder(orderId)
        }
    }
    
    @Test
    @DisplayName("존재하지 않는 주문을 조회하면 예외가 발생한다")
    fun getOrderWithNonExistingId() {
        // given
        val orderId = "non-existing-order"
        
        whenever(orderService.getOrderById(any())).thenThrow(OrderException.NotFound("주문을 찾을 수 없습니다"))
        
        // when, then
        assertThrows<OrderException.NotFound> {
            orderFacade.getOrder(orderId)
        }
    }
    
    @Test
    @DisplayName("사용자 ID로 주문 목록을 조회한다")
    fun getOrdersByUserId() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        
        val orders = listOf(
            Order(
                orderId = "order1",
                userId = userId,
                paymentId = "payment1",
                totalAmount = 10000,
                totalDiscountAmount = 1000,
                finalAmount = 9000
            ),
            Order(
                orderId = "order2",
                userId = userId,
                paymentId = "payment2",
                totalAmount = 20000,
                totalDiscountAmount = 2000,
                finalAmount = 18000
            )
        )
        
        val orderItems1 = listOf(
            OrderItem(
                orderItemId = "orderItem1",
                orderId = "order1",
                productId = "product1",
                amount = 2,
                unitPrice = 5000,
                totalPrice = 10000
            )
        )
        
        val orderItems2 = listOf(
            OrderItem(
                orderItemId = "orderItem2",
                orderId = "order2",
                productId = "product2",
                amount = 4,
                unitPrice = 5000,
                totalPrice = 20000
            )
        )
        
        val orderDiscounts1 = listOf(
            OrderDiscount(
                orderDiscountId = "orderDiscount1",
                orderId = "order1",
                discountType = DiscountType.COUPON,
                discountId = "coupon1",
                discountAmount = 1000
            )
        )
        
        val orderDiscounts2 = listOf(
            OrderDiscount(
                orderDiscountId = "orderDiscount2",
                orderId = "order2",
                discountType = DiscountType.COUPON,
                discountId = "coupon2",
                discountAmount = 2000
            )
        )
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(orderService.getOrdersByUserId(any())).thenReturn(orders)
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order1"))).thenReturn(orderItems1)
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order2"))).thenReturn(orderItems2)
        whenever(orderService.getOrderDiscountsByOrderId(OrderDiscountCommand.GetByOrderId("order1"))).thenReturn(orderDiscounts1)
        whenever(orderService.getOrderDiscountsByOrderId(OrderDiscountCommand.GetByOrderId("order2"))).thenReturn(orderDiscounts2)
        
        // when
        val result = orderFacade.getOrdersByUserId(userId)
        
        // then
        assertThat(result.orders).hasSize(2)
        assertThat(result.orders[0].orderId).isEqualTo("order1")
        assertThat(result.orders[0].items).hasSize(1)
        assertThat(result.orders[0].discounts).hasSize(1)
        assertThat(result.orders[1].orderId).isEqualTo("order2")
        assertThat(result.orders[1].items).hasSize(1)
        assertThat(result.orders[1].discounts).hasSize(1)
    }
    
    @Test
    @DisplayName("빈 사용자 ID로 주문 목록을 조회하면 예외가 발생한다")
    fun getOrdersByUserIdWithBlankUserId() {
        // given
        val userId = ""
        
        // when, then
        assertThrows<UserException.UserIdShouldNotBlank> {
            orderFacade.getOrdersByUserId(userId)
        }
    }
    

    @Test
    @DisplayName("주문을 생성한다")
    fun createOrder() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        
        val product1 = Product(
            productId = "product1",
            name = "상품1",
            price = 5000,
            stock = 10
        )
        
        val product2 = Product(
            productId = "product2",
            name = "상품2",
            price = 3000,
            stock = 20
        )
        
        val orderItems = listOf(
            OrderItemRequest(productId = "product1", amount = 2),
            OrderItemRequest(productId = "product2", amount = 3)
        )
        
        val payment = Payment(
            paymentId = "payment1",
            userId = userId,
            totalPaymentAmount = 19000
        )
        
        val order = Order(
            orderId = "order1",
            userId = userId,
            paymentId = "payment1",
            totalAmount = 19000,
            totalDiscountAmount = 0,
            finalAmount = 19000
        )
        
        val orderItemsResult = listOf(
            OrderItem(
                orderItemId = "orderItem1",
                orderId = "order1",
                productId = "product1",
                amount = 2,
                unitPrice = 5000,
                totalPrice = 10000
            ),
            OrderItem(
                orderItemId = "orderItem2",
                orderId = "order1",
                productId = "product2",
                amount = 3,
                unitPrice = 3000,
                totalPrice = 9000
            )
        )
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(productService.getProductById(ProductQuery.GetById("product1"))).thenReturn(product1)
        whenever(productService.getProductById(ProductQuery.GetById("product2"))).thenReturn(product2)
        whenever(productService.decreaseProductStock(any())).thenReturn(product1)
        whenever(paymentService.createPayment(any())).thenReturn(payment)
        whenever(orderService.createOrder(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(any())).thenReturn(orderItemsResult)
        whenever(orderService.getOrderDiscountsByOrderId(any())).thenReturn(emptyList())
        
        // when
        val result = orderFacade.createOrder(userId, orderItems)
        
        // then
        assertThat(result.orderId).isEqualTo("order1")
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.paymentId).isEqualTo("payment1")
        assertThat(result.totalAmount).isEqualTo(19000)
        assertThat(result.finalAmount).isEqualTo(19000)
        assertThat(result.items).hasSize(2)
    }
    
    @Test
    @DisplayName("쿠폰을 적용하여 주문을 생성한다")
    fun createOrderWithCoupon() {
        // given
        val userId = "user1"
        val couponUserId = "coupon1"
        val user = User(userId = userId)
        
        val product = Product(
            productId = "product1",
            name = "상품1",
            price = 5000,
            stock = 10
        )
        
        val orderItems = listOf(
            OrderItemRequest(productId = "product1", amount = 2)
        )
        
        val payment = Payment(
            paymentId = "payment1",
            userId = userId,
            totalPaymentAmount = 9000
        )
        
        val order = Order(
            orderId = "order1",
            userId = userId,
            paymentId = "payment1",
            totalAmount = 10000,
            totalDiscountAmount = 1000,
            finalAmount = 9000
        )
        
        val orderItemsResult = listOf(
            OrderItem(
                orderItemId = "orderItem1",
                orderId = "order1",
                productId = "product1",
                amount = 2,
                unitPrice = 5000,
                totalPrice = 10000
            )
        )
        
        val orderDiscountsResult = listOf(
            OrderDiscount(
                orderDiscountId = "orderDiscount1",
                orderId = "order1",
                discountType = DiscountType.COUPON,
                discountId = couponUserId,
                discountAmount = 1000
            )
        )
        
        val couponUser = mock<CouponUser>()
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(productService.getProductById(ProductQuery.GetById("product1"))).thenReturn(product)
        whenever(productService.decreaseProductStock(any())).thenReturn(product)
        whenever(couponUserService.getCouponUser(CouponUserCommand.GetById(couponUserId))).thenReturn(couponUser)
        whenever(couponUser.calculateDiscountAmount(10000)).thenReturn(1000)
        whenever(couponUserService.useCoupon(CouponUserCommand.Use(couponUserId))).thenReturn(couponUser)
        whenever(paymentService.createPayment(any())).thenReturn(payment)
        whenever(orderService.createOrder(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(any())).thenReturn(orderItemsResult)
        whenever(orderService.getOrderDiscountsByOrderId(any())).thenReturn(orderDiscountsResult)
        
        // when
        val result = orderFacade.createOrder(userId, orderItems, couponUserId)
        
        // then
        assertThat(result.orderId).isEqualTo("order1")
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.totalAmount).isEqualTo(10000)
        assertThat(result.totalDiscountAmount).isEqualTo(1000)
        assertThat(result.finalAmount).isEqualTo(9000)
        assertThat(result.items).hasSize(1)
        assertThat(result.discounts).hasSize(1)
        assertThat(result.discounts[0].discountType).isEqualTo(DiscountType.COUPON)
        assertThat(result.discounts[0].discountAmount).isEqualTo(1000)
    }
    
    @Test
    @DisplayName("상품 재고가 부족하면 예외가 발생한다")
    fun createOrderWithInsufficientStock() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        
        val product = Product(
            productId = "product1",
            name = "상품1",
            price = 5000,
            stock = 1
        )
        
        val orderItems = listOf(
            OrderItemRequest(productId = "product1", amount = 2)
        )
        
        whenever(userService.getUserById(any())).thenReturn(user)
        whenever(productService.getProductById(ProductQuery.GetById("product1"))).thenReturn(product)
        whenever(productService.decreaseProductStock(any())).thenThrow(ProductException.StockAmountUnderflow("재고 부족"))
        
        // when, then
        assertThrows<ProductException.StockAmountUnderflow> {
            orderFacade.createOrder(userId, orderItems)
        }
    }
    
    @Test
    @DisplayName("주문 항목이 없으면 예외가 발생한다")
    fun createOrderWithNoItems() {
        // given
        val userId = "user1"
        val user = User(userId = userId)
        
        val orderItems = emptyList<OrderItemRequest>()
        
        whenever(userService.getUserById(any())).thenReturn(user)
        
        // when, then
        assertThrows<OrderException.OrderItemRequired> {
            orderFacade.createOrder(userId, orderItems)
        }
    }
} 