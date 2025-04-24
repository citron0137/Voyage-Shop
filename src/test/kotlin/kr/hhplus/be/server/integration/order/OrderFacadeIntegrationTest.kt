package kr.hhplus.be.server.integration.order

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.order.OrderCriteria
import kr.hhplus.be.server.application.order.OrderFacade
import kr.hhplus.be.server.domain.couponuser.CouponUserBenefitMethod
import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.UserCommand
import kr.hhplus.be.server.domain.user.UserService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@DisplayName("주문 파사드 통합 테스트")
class OrderFacadeIntegrationTest {

    @Autowired
    private lateinit var orderFacade: OrderFacade

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var couponUserService: CouponUserService

    private lateinit var testUserId: String
    private lateinit var testProductId: String

    @BeforeEach
    fun setUp() {
        // 테스트용 사용자 생성
        val user = userService.createUser(UserCommand.Create)
        testUserId = user.userId

        // 테스트용 상품 생성
        val productCommand = ProductCommand.Create(
            name = "테스트 상품",
            price = 10000,
            stock = 100
        )
        val product = productService.createProduct(productCommand)
        testProductId = product.productId
    }

    @Test
    @DisplayName("주문을 생성할 수 있다")
    @Transactional
    fun createOrderTest() {
        // given
        val orderCriteria = OrderCriteria.Create(
            userId = testUserId,
            items = listOf(
                OrderCriteria.Create.OrderItem(
                    productId = testProductId,
                    amount = 2
                )
            )
        )

        // when
        val result = orderFacade.createOrder(orderCriteria)

        // then
        assertThat(result.orderId).isNotBlank()
        assertThat(result.userId).isEqualTo(testUserId)
        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].productId).isEqualTo(testProductId)
        assertThat(result.items[0].amount).isEqualTo(2)
        assertThat(result.totalAmount).isEqualTo(20000) // 10000 * 2
        assertThat(result.finalAmount).isEqualTo(20000) // 할인 없음
    }

    @Test
    @DisplayName("주문을 조회할 수 있다")
    @Transactional
    fun getOrderTest() {
        // given
        // 주문 생성
        val createCriteria = OrderCriteria.Create(
            userId = testUserId,
            items = listOf(
                OrderCriteria.Create.OrderItem(
                    productId = testProductId,
                    amount = 1
                )
            )
        )
        val createdOrder = orderFacade.createOrder(createCriteria)

        // when
        val result = orderFacade.getOrder(OrderCriteria.GetById(createdOrder.orderId))

        // then
        assertThat(result.orderId).isEqualTo(createdOrder.orderId)
        assertThat(result.userId).isEqualTo(testUserId)
        assertThat(result.items).hasSize(1)
        assertThat(result.items[0].productId).isEqualTo(testProductId)
    }

    @Test
    @DisplayName("사용자 ID로 주문 목록을 조회할 수 있다")
    @Transactional
    fun getOrdersByUserIdTest() {
        // given
        // 첫 번째 주문 생성
        orderFacade.createOrder(
            OrderCriteria.Create(
                userId = testUserId,
                items = listOf(
                    OrderCriteria.Create.OrderItem(
                        productId = testProductId,
                        amount = 1
                    )
                )
            )
        )

        // 두 번째 주문 생성
        orderFacade.createOrder(
            OrderCriteria.Create(
                userId = testUserId,
                items = listOf(
                    OrderCriteria.Create.OrderItem(
                        productId = testProductId,
                        amount = 2
                    )
                )
            )
        )

        // when
        val result = orderFacade.getOrdersByUserId(OrderCriteria.GetByUserId(testUserId))

        // then
        assertThat(result.orders).hasSize(2)
        assertThat(result.orders[0].userId).isEqualTo(testUserId)
        assertThat(result.orders[1].userId).isEqualTo(testUserId)
    }

    @Test
    @DisplayName("모든 주문 목록을 조회할 수 있다")
    @Transactional
    fun getAllOrdersTest() {
        // given
        val initialCount = orderFacade.getAllOrders(OrderCriteria.GetAll).orders.size

        // 주문 생성
        orderFacade.createOrder(
            OrderCriteria.Create(
                userId = testUserId,
                items = listOf(
                    OrderCriteria.Create.OrderItem(
                        productId = testProductId,
                        amount = 1
                    )
                )
            )
        )

        // when
        val result = orderFacade.getAllOrders(OrderCriteria.GetAll)

        // then
        assertThat(result.orders.size).isEqualTo(initialCount + 1)
    }

    @Test
    @DisplayName("쿠폰을 적용하여 주문을 할인 받을 수 있다")
    @Transactional
    fun createOrderWithCouponTest() {
        // given
        // 테스트용 쿠폰 생성 (고정 금액 1000원 할인)
        val couponCommand = CouponUserCommand.Create(
            userId = testUserId,
            benefitMethod = CouponUserBenefitMethod.DISCOUNT_FIXED_AMOUNT,
            benefitAmount = "1000"
        )
        val couponUser = couponUserService.create(couponCommand)

        // 주문 생성 기준 설정
        val orderCriteria = OrderCriteria.Create(
            userId = testUserId,
            items = listOf(
                OrderCriteria.Create.OrderItem(
                    productId = testProductId,
                    amount = 2
                )
            ),
            couponUserId = couponUser.couponUserId
        )

        // when
        val result = orderFacade.createOrder(orderCriteria)

        // then
        assertThat(result.orderId).isNotBlank()
        assertThat(result.totalAmount).isEqualTo(20000) // 10000 * 2
        assertThat(result.discounts).hasSize(1)
        assertThat(result.finalAmount).isEqualTo(19000) // 20000 - 1000
    }
} 