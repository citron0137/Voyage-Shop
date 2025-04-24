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
import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.user.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Assertions.assertEquals
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import org.mockito.kotlin.times
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doThrow

@DisplayName("OrderFacade 테스트")
class OrderFacadeTest {
    
    private lateinit var orderService: OrderService
    private lateinit var productService: ProductService
    private lateinit var paymentService: PaymentService
    private lateinit var couponUserService: CouponUserService
    private lateinit var orderFacade: OrderFacade
    
    @BeforeEach
    fun setup() {
        orderService = mock()
        productService = mock()
        paymentService = mock()
        couponUserService = mock()
        orderFacade = OrderFacade(
            orderService = orderService,
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
                type = OrderDiscountType.COUPON,
                discountId = "coupon1",
                discountAmount = 1000
            )
        )
        
        whenever(orderService.getOrderById(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(any())).thenReturn(orderItems)
        whenever(orderService.getOrderDiscountsByOrderId(any())).thenReturn(orderDiscounts)
        
        // when
        val result = orderFacade.getOrder(OrderCriteria.GetById(orderId))
        
        // then
        assertThat(result.orderId).isEqualTo(orderId)
        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.totalAmount).isEqualTo(10000)
        assertThat(result.totalDiscountAmount).isEqualTo(1000)
        assertThat(result.finalAmount).isEqualTo(9000)
        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].productId).isEqualTo("product1")
        assertThat(result.discounts).hasSize(1)
        assertThat(result.discounts[0].orderDiscountType).isEqualTo(OrderDiscountType.COUPON)
    }
    
    @Test
    @DisplayName("빈 주문 ID로 조회하면 예외가 발생한다")
    fun getOrderWithBlankId() {
        // given
        val orderId = ""
        
        // when, then
        assertThrows<OrderException.OrderIdShouldNotBlank> {
            orderFacade.getOrder(OrderCriteria.GetById(orderId))
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
            orderFacade.getOrder(OrderCriteria.GetById(orderId))
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
                type = OrderDiscountType.COUPON,
                discountId = "coupon1",
                discountAmount = 1000
            )
        )
        
        val orderDiscounts2 = listOf(
            OrderDiscount(
                orderDiscountId = "orderDiscount2",
                orderId = "order2",
                type = OrderDiscountType.COUPON,
                discountId = "coupon2",
                discountAmount = 2000
            )
        )
        
        whenever(orderService.getOrdersByUserId(any())).thenReturn(orders)
        whenever(orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId("order1"))).thenReturn(orderItems1)
        whenever(orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId("order2"))).thenReturn(orderItems2)
        whenever(orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId("order1"))).thenReturn(orderDiscounts1)
        whenever(orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId("order2"))).thenReturn(orderDiscounts2)
        
        // when
        val result = orderFacade.getOrdersByUserId(OrderCriteria.GetByUserId(userId))
        
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
        assertThrows<OrderException.UserIdShouldNotBlank> {
            orderFacade.getOrdersByUserId(OrderCriteria.GetByUserId(userId))
        }
    }
    
    @Test
    @DisplayName("주문을 생성한다")
    fun createOrder() {
        // given
        val userId = "user123"
        val productId1 = "product1"
        val productId2 = "product2"
        val amount1 = 2L
        val amount2 = 1L
        val price1 = 1000L
        val price2 = 2000L
        val couponUserId = "coupon123"
        val couponDiscountAmount = 500L
        val paymentId = "payment123"
        val orderId = "order123"
        
        // 테스트용 주문 생성 기준 객체
        val orderCriteria = OrderCriteria.Create(
            userId = userId,
            items = listOf(
                OrderCriteria.Create.OrderItem(productId1, amount1),
                OrderCriteria.Create.OrderItem(productId2, amount2)
            ),
            couponUserId = couponUserId
        )
        
        // 상품 정보
        val product1 = Product(
            productId = productId1,
            name = "상품 1",
            price = price1,
            stock = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val product2 = Product(
            productId = productId2,
            name = "상품 2",
            price = price2,
            stock = 5,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 쿠폰 정보
        val couponUser = mock<CouponUser>()
        
        // 결제 정보
        val payment = Payment(
            paymentId = paymentId,
            userId = userId,
            totalPaymentAmount = (price1 * amount1 + price2 * amount2) - couponDiscountAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 생성된 주문
        val order = Order(
            orderId = orderId,
            userId = userId,
            paymentId = paymentId,
            totalAmount = price1 * amount1 + price2 * amount2,
            totalDiscountAmount = couponDiscountAmount,
            finalAmount = (price1 * amount1 + price2 * amount2) - couponDiscountAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 생성된 주문 항목
        val orderItem1 = OrderItem(
            orderItemId = "orderItem1",
            orderId = orderId,
            productId = productId1,
            amount = amount1,
            unitPrice = price1,
            totalPrice = amount1 * price1,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val orderItem2 = OrderItem(
            orderItemId = "orderItem2",
            orderId = orderId,
            productId = productId2,
            amount = amount2,
            unitPrice = price2,
            totalPrice = amount2 * price2,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val orderItems = listOf(orderItem1, orderItem2)
        
        // 생성된 주문 할인
        val orderDiscount = OrderDiscount(
            orderDiscountId = "orderDiscount1",
            orderId = orderId,
            type = OrderDiscountType.COUPON,
            discountId = couponUserId,
            discountAmount = couponDiscountAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val orderDiscounts = listOf(orderDiscount)

        val product1DecreaseStockAmountCommand = ProductCommand.DecreaseStock(productId1, amount1)
        val product2DecreaseStockAmountCommand = ProductCommand.DecreaseStock(productId2, amount2)
        val product1AfterOrder = Product(
            productId = productId1,
            name = "상품 1",
            price = price1,
            stock = 8,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val product2AfterOrder = Product(
            productId = productId2,
            name = "상품 2",
            price = price2,
            stock = 4,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        // when
        // ProductService의 메소드 모킹
        whenever(productService.getProductById(ProductQuery.GetById(productId1))).thenReturn(product1)
        whenever(productService.getProductById(ProductQuery.GetById(productId2))).thenReturn(product2)
        whenever(productService.decreaseProductStock(product1DecreaseStockAmountCommand)).thenReturn(product1AfterOrder)
        whenever(productService.decreaseProductStock(product2DecreaseStockAmountCommand)).thenReturn(product2AfterOrder)
        
        // CouponUserService의 메소드 모킹
        whenever(couponUserService.getCouponUser(CouponUserCommand.GetById(couponUserId))).thenReturn(couponUser)
        whenever(couponUser.calculateDiscountAmount(any())).thenReturn(couponDiscountAmount)
        whenever(couponUserService.useCoupon(any())).thenReturn(couponUser)
        
        // PaymentService의 메소드 모킹
        whenever(paymentService.createPayment(any())).thenReturn(payment)
        
        // OrderService의 메소드 모킹
        whenever(orderService.createOrder(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(orderId))).thenReturn(orderItems)
        whenever(orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(orderId))).thenReturn(orderDiscounts)
        
        // 테스트 대상 메소드 호출
        val result = orderFacade.createOrder(orderCriteria)
        
        // then
        // 상품 관련 메소드 호출 검증
        verify(productService, times(2)).getProductById(any())
        verify(productService, times(2)).decreaseProductStock(any())
        
        // 쿠폰 관련 메소드 호출 검증
        verify(couponUserService).getCouponUser(CouponUserCommand.GetById(couponUserId))
        verify(couponUser).calculateDiscountAmount(any())
        verify(couponUserService).useCoupon(any())
        
        // 결제 관련 메소드 호출 검증
        verify(paymentService).createPayment(any())
        
        // 주문 관련 메소드 호출 검증
        verify(orderService).createOrder(any())
        verify(orderService).getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(orderId))
        verify(orderService).getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(orderId))
        
        // 반환 결과 검증
        assertEquals(orderId, result.orderId)
        assertEquals(userId, result.userId)
        assertEquals(paymentId, result.paymentId)
        assertEquals(price1 * amount1 + price2 * amount2, result.totalAmount)
        assertEquals(couponDiscountAmount, result.totalDiscountAmount)
        assertEquals((price1 * amount1 + price2 * amount2) - couponDiscountAmount, result.finalAmount)
        assertEquals(2, result.items.size)
        assertEquals(1, result.discounts.size)
    }
    
    @Test
    @DisplayName("쿠폰을 적용하여 주문을 생성한다")
    fun createOrderWithCoupon() {
        // given
        val userId = "user123"
        val productId = "product1"
        val amount = 2L
        val price = 10000L
        val couponUserId = "coupon123"
        val couponDiscountAmount = 2000L
        val paymentId = "payment123"
        val orderId = "order123"
        
        // 계산된 금액 정의
        val totalAmount = price * amount
        val totalDiscountAmount = couponDiscountAmount
        val finalAmount = totalAmount - totalDiscountAmount
        
        // 쿠폰이 적용된 주문 생성 기준 객체
        val orderCriteria = OrderCriteria.Create(
            userId = userId,
            items = listOf(
                OrderCriteria.Create.OrderItem(productId, amount)
            ),
            couponUserId = couponUserId
        )
        
        // 상품 정보
        val product = Product(
            productId = productId,
            name = "상품 1",
            price = price,
            stock = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 쿠폰 정보
        val couponUser = mock<CouponUser>()
        
        // 결제 정보 (쿠폰 할인 적용됨)
        val payment = Payment(
            paymentId = paymentId,
            userId = userId,
            totalPaymentAmount = finalAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 생성된 주문
        val order = Order(
            orderId = orderId,
            userId = userId,
            paymentId = paymentId,
            totalAmount = totalAmount,
            totalDiscountAmount = totalDiscountAmount,
            finalAmount = finalAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 생성된 주문 항목
        val orderItem = OrderItem(
            orderItemId = "orderItem1",
            orderId = orderId,
            productId = productId,
            amount = amount,
            unitPrice = price,
            totalPrice = amount * price,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // 생성된 주문 할인 (쿠폰 적용)
        val orderDiscount = OrderDiscount(
            orderDiscountId = "orderDiscount1",
            orderId = orderId,
            type = OrderDiscountType.COUPON,
            discountId = couponUserId,
            discountAmount = couponDiscountAmount,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // when
        // 상품 서비스 모킹
        whenever(productService.getProductById(any())).thenReturn(product)
//        doNothing().whenever(productService).decreaseProductStock(any())
        
        // 쿠폰 서비스 모킹 - 쿠폰 할인 적용
        whenever(couponUserService.getCouponUser(any())).thenReturn(couponUser)
        whenever(couponUser.calculateDiscountAmount(any())).thenReturn(couponDiscountAmount)
//        doNothing().whenever(couponUserService).useCoupon(any())
        
        // 결제 및 주문 서비스 모킹
        whenever(paymentService.createPayment(any())).thenReturn(payment)
        whenever(orderService.createOrder(any())).thenReturn(order)
        whenever(orderService.getOrderItemsByOrderId(any())).thenReturn(listOf(orderItem))
        whenever(orderService.getOrderDiscountsByOrderId(any())).thenReturn(listOf(orderDiscount))
        
        // 테스트 대상 메소드 호출
        val result = orderFacade.createOrder(orderCriteria)
        
        // then
        // 쿠폰 관련 검증 - 쿠폰이 사용되었는지 확인
        verify(couponUserService).getCouponUser(any())
        verify(couponUser).calculateDiscountAmount(any())
        verify(couponUserService).useCoupon(any())
        
        // 주문 결과 검증
        assertEquals(orderId, result.orderId)
        assertEquals(totalAmount, price * amount)
        assertEquals(totalDiscountAmount, couponDiscountAmount)
        assertEquals(finalAmount, price * amount - couponDiscountAmount)
        assertEquals(1, result.discounts.size)
        assertEquals(OrderDiscountType.COUPON, result.discounts[0].orderDiscountType)
        assertEquals(couponDiscountAmount, result.discounts[0].discountAmount)
    }
    
    @Test
    @DisplayName("상품 재고가 부족하면 예외가 발생한다")
    fun createOrderWithInsufficientStock() {
        // given
        val userId = "user123"
        val productId = "product1"
        val amount = 5L // 주문 수량
        
        // 재고 부족 상황의 주문 생성 기준 객체
        val orderCriteria = OrderCriteria.Create(
            userId = userId,
            items = listOf(
                OrderCriteria.Create.OrderItem(productId, amount)
            )
        )
        
        // 상품 정보 (재고가 3개로 주문 수량인 5개보다 적음)
        val product = Product(
            productId = productId,
            name = "상품 1",
            price = 1000,
            stock = 3, // 재고가 부족함
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        // when
        // 상품 조회는 성공하지만, 재고 감소 시 예외 발생하도록 설정
        whenever(productService.getProductById(any())).thenReturn(product)
        doThrow(ProductException.StockAmountUnderflow("상품 ${productId}의 재고가 부족합니다."))
            .whenever(productService).decreaseProductStock(any())
        
        // then
        assertThrows<ProductException.StockAmountUnderflow> {
            orderFacade.createOrder(orderCriteria)
        }
        
        // 재고 감소 시도 검증
        verify(productService).decreaseProductStock(any())
    }
}