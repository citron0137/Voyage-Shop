package kr.hhplus.be.server.application.orderitemrank

import kr.hhplus.be.server.domain.order.Order
import kr.hhplus.be.server.domain.order.OrderItem
import kr.hhplus.be.server.domain.order.OrderItemCommand
import kr.hhplus.be.server.domain.order.OrderService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@DisplayName("OrderItemRankFacade 테스트")
class OrderItemRankFacadeTest {
    
    private lateinit var orderService: OrderService
    private lateinit var orderItemRankFacade: OrderItemRankFacade
    
    @BeforeEach
    fun setup() {
        orderService = mock()
        orderItemRankFacade = OrderItemRankFacade(orderService)
    }
    
    @Test
    @DisplayName("최근 3일간 주문 아이템 중 상위 5개를 조회한다")
    fun getRecentTopOrderItemRanks() {
        // given
        val now = LocalDateTime.now()
        val threeDaysAgo = now.minusDays(3)
        
        // 최근 주문
        val recentOrder1 = Order(
            orderId = "order1",
            userId = "user1",
            paymentId = "payment1",
            totalAmount = 10000,
            totalDiscountAmount = 0,
            finalAmount = 10000,
            createdAt = now.minusDays(1)
        )
        
        val recentOrder2 = Order(
            orderId = "order2",
            userId = "user2",
            paymentId = "payment2",
            totalAmount = 20000,
            totalDiscountAmount = 0,
            finalAmount = 20000,
            createdAt = now.minusDays(2)
        )
        
        // 3일 이전 주문
        val oldOrder = Order(
            orderId = "order3",
            userId = "user3",
            paymentId = "payment3",
            totalAmount = 30000,
            totalDiscountAmount = 0,
            finalAmount = 30000,
            createdAt = now.minusDays(4)
        )
        
        // 주문 아이템들
        val order1Items = listOf(
            OrderItem(
                orderItemId = "item1",
                orderId = "order1",
                productId = "product1",
                amount = 3,
                unitPrice = 1000,
                totalPrice = 3000
            ),
            OrderItem(
                orderItemId = "item2",
                orderId = "order1",
                productId = "product2",
                amount = 2,
                unitPrice = 2000,
                totalPrice = 4000
            ),
            OrderItem(
                orderItemId = "item3",
                orderId = "order1",
                productId = "product3",
                amount = 1,
                unitPrice = 3000,
                totalPrice = 3000
            )
        )
        
        val order2Items = listOf(
            OrderItem(
                orderItemId = "item4",
                orderId = "order2",
                productId = "product1",
                amount = 5,
                unitPrice = 1000,
                totalPrice = 5000
            ),
            OrderItem(
                orderItemId = "item5",
                orderId = "order2",
                productId = "product4",
                amount = 3,
                unitPrice = 5000,
                totalPrice = 15000
            )
        )
        
        val order3Items = listOf(
            OrderItem(
                orderItemId = "item6",
                orderId = "order3",
                productId = "product5",
                amount = 10,
                unitPrice = 3000,
                totalPrice = 30000
            )
        )
        
        whenever(orderService.getAllOrders()).thenReturn(listOf(recentOrder1, recentOrder2, oldOrder))
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order1"))).thenReturn(order1Items)
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order2"))).thenReturn(order2Items)
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order3"))).thenReturn(order3Items)
        
        // when
        val result = orderItemRankFacade.getRecentTopOrderItemRanks()
        
        // then
        // 예상 결과: product1(8), product4(3), product2(2), product3(1) - 상위 4개만 (3일 이내 주문에서)
        assertThat(result).hasSize(4)
        
        // 1위: product1 (총 8개 주문)
        assertThat(result[0].productId).isEqualTo("product1")
        assertThat(result[0].orderCount).isEqualTo(8)
        
        // 2위: product4 (총 3개 주문)
        assertThat(result[1].productId).isEqualTo("product4")
        assertThat(result[1].orderCount).isEqualTo(3)
        
        // 3위: product2 (총 2개 주문)
        assertThat(result[2].productId).isEqualTo("product2")
        assertThat(result[2].orderCount).isEqualTo(2)
        
        // 4위: product3 (총 1개 주문)
        assertThat(result[3].productId).isEqualTo("product3")
        assertThat(result[3].orderCount).isEqualTo(1)
        
        // product5는 3일 이전 주문이므로 결과에 포함되지 않음
    }
    
    @Test
    @DisplayName("최근 3일간 주문이 없으면 빈 리스트를 반환한다")
    fun getRecentTopOrderItemRanksWithNoRecentOrders() {
        // given
        val now = LocalDateTime.now()
        
        // 모든 주문이 3일 이전
        val oldOrder1 = Order(
            orderId = "order1",
            userId = "user1",
            paymentId = "payment1",
            totalAmount = 10000,
            totalDiscountAmount = 0,
            finalAmount = 10000,
            createdAt = now.minusDays(4)
        )
        
        val oldOrder2 = Order(
            orderId = "order2",
            userId = "user2",
            paymentId = "payment2",
            totalAmount = 20000,
            totalDiscountAmount = 0,
            finalAmount = 20000,
            createdAt = now.minusDays(5)
        )
        
        val order1Items = listOf(
            OrderItem(
                orderItemId = "item1",
                orderId = "order1",
                productId = "product1",
                amount = 3,
                unitPrice = 1000,
                totalPrice = 3000
            )
        )
        
        val order2Items = listOf(
            OrderItem(
                orderItemId = "item2",
                orderId = "order2",
                productId = "product2",
                amount = 2,
                unitPrice = 2000,
                totalPrice = 4000
            )
        )
        
        whenever(orderService.getAllOrders()).thenReturn(listOf(oldOrder1, oldOrder2))
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order1"))).thenReturn(order1Items)
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order2"))).thenReturn(order2Items)
        
        // when
        val result = orderItemRankFacade.getRecentTopOrderItemRanks()
        
        // then
        assertThat(result).isEmpty()
    }
    
    @Test
    @DisplayName("상위 5개 이상의 상품이 있을 경우 상위 5개만 반환한다")
    fun getRecentTopOrderItemRanksWithMoreThan5Products() {
        // given
        val now = LocalDateTime.now()
        
        val recentOrder = Order(
            orderId = "order1",
            userId = "user1",
            paymentId = "payment1",
            totalAmount = 60000,
            totalDiscountAmount = 0,
            finalAmount = 60000,
            createdAt = now.minusDays(1)
        )
        
        // 6개 상품을 포함한 주문
        val orderItems = listOf(
            OrderItem(orderItemId = "item1", orderId = "order1", productId = "product1", amount = 10, unitPrice = 1000, totalPrice = 10000),
            OrderItem(orderItemId = "item2", orderId = "order1", productId = "product2", amount = 9, unitPrice = 1000, totalPrice = 9000),
            OrderItem(orderItemId = "item3", orderId = "order1", productId = "product3", amount = 8, unitPrice = 1000, totalPrice = 8000),
            OrderItem(orderItemId = "item4", orderId = "order1", productId = "product4", amount = 7, unitPrice = 1000, totalPrice = 7000),
            OrderItem(orderItemId = "item5", orderId = "order1", productId = "product5", amount = 6, unitPrice = 1000, totalPrice = 6000),
            OrderItem(orderItemId = "item6", orderId = "order1", productId = "product6", amount = 5, unitPrice = 1000, totalPrice = 5000)
        )
        
        whenever(orderService.getAllOrders()).thenReturn(listOf(recentOrder))
        whenever(orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId("order1"))).thenReturn(orderItems)
        
        // when
        val result = orderItemRankFacade.getRecentTopOrderItemRanks()
        
        // then
        assertThat(result).hasSize(5)
        
        // 순서대로 확인
        assertThat(result[0].productId).isEqualTo("product1")
        assertThat(result[0].orderCount).isEqualTo(10)
        
        assertThat(result[1].productId).isEqualTo("product2")
        assertThat(result[1].orderCount).isEqualTo(9)
        
        assertThat(result[2].productId).isEqualTo("product3")
        assertThat(result[2].orderCount).isEqualTo(8)
        
        assertThat(result[3].productId).isEqualTo("product4")
        assertThat(result[3].orderCount).isEqualTo(7)
        
        assertThat(result[4].productId).isEqualTo("product5")
        assertThat(result[4].orderCount).isEqualTo(6)
        
        // product6은 6위이므로 결과에 포함되지 않음
    }
} 