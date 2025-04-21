package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.UserException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 파사드
 * 주문 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class OrderFacade(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
    private val couponUserService: CouponUserService
) {

    /**
     * 주문 ID로 주문 정보를 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 정보
     * @throws OrderException.OrderIdShouldNotBlank 주문 ID가 빈 값인 경우
     * @throws OrderException.NotFound 주문을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getOrder(criteria: OrderCriteria.GetById): OrderResult.Single {
        val order = orderService.getOrderById(criteria.toQuery())
        
        // 주문 항목 조회
        val items = orderService.getOrderItemsByOrderId(criteria.toItemsQuery())
        
        // 주문 할인 조회
        val discounts = orderService.getOrderDiscountsByOrderId(criteria.toDiscountsQuery())
        
        return OrderResult.Single.from(order, items, discounts)
    }
    
    /**
     * 사용자 ID로 주문 목록을 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 목록
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getOrdersByUserId(criteria: OrderCriteria.GetByUserId): OrderResult.List {
        val orders = orderService.getOrdersByUserId(criteria.toQuery())
        
        // 주문이 없으면 빈 목록 반환
        if (orders.isEmpty()) {
            return OrderResult.List(emptyList())
        }
        
        // 각 주문의 항목과 할인 정보 조회
        val orderIds = orders.map { it.orderId }
        val allItems = orderIds.flatMap { 
            orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(it)) 
        }
        val allDiscounts = orderIds.flatMap { 
            orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(it)) 
        }
        
        // 주문 ID별로 그룹화
        val itemsByOrderId = allItems.groupBy { it.orderId }
        val discountsByOrderId = allDiscounts.groupBy { it.orderId }
        
        return OrderResult.List.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
    }
    

    /**
     * 모든 주문 목록을 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 목록
     */
    @Transactional(readOnly = true)
    fun getAllOrders(criteria: OrderCriteria.GetAll): OrderResult.List {
        val orders = orderService.getAllOrders(criteria.toQuery())
        
        // 주문이 없으면 빈 목록 반환
        if (orders.isEmpty()) {
            return OrderResult.List(emptyList())
        }
        
        // 각 주문의 항목과 할인 정보 조회
        val orderIds = orders.map { it.orderId }
        val allItems = orderIds.flatMap { 
            orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(it))
        }
        val allDiscounts = orderIds.flatMap { 
            orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(it))
        }
        
        // 주문 ID별로 그룹화
        val itemsByOrderId = allItems.groupBy { it.orderId }
        val discountsByOrderId = allDiscounts.groupBy { it.orderId }
        
        return OrderResult.List.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
    }
    
    /**
     * 새로운 주문을 생성합니다.
     *
     * @param criteria 주문 생성 기준
     * @return 생성된 주문 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountUnderflow 상품 재고가 부족한 경우
     * @throws OrderException.OrderItemRequired 주문 항목이 없는 경우
     * @throws OrderException.FinalAmountShouldMoreThan0 최종 결제 금액이 0 이하인 경우
     */
    @Transactional
    fun createOrder(criteria: OrderCriteria.Create): OrderResult.Single {
        // 1. 재고 차감
        val decreaseStockCommands = criteria.toDecreaseStockAmountCommands()
        decreaseStockCommands.forEach { productService.decreaseProductStock(it)}

        // 2. 총 금액 계산
        val getProductQueries = criteria.toGetProductQueries()
        val products = getProductQueries.map { productService.getProductById(it) }
        val totalPrice = criteria.items.sumOf { item -> 
            item.amount * products.find { it.productId == item.productId }!!.price 
        }

        // 3. 쿠폰 적용 
        val couponDiscountPrice = if (criteria.couponUserId == null) {0L} else {
            couponUserService.useCoupon(CouponUserCommand.Use(criteria.couponUserId))
            val couponUser = couponUserService.getCouponUser(CouponUserCommand.GetById(criteria.couponUserId))
            couponUser.calculateDiscountAmount(totalPrice)
        }


        // 최종 결제 금액 계산
        val finalAmount = totalPrice - couponDiscountPrice
        if (finalAmount <= 0) {
            throw OrderException.FinalAmountShouldMoreThan0("최종 결제 금액은 0보다 커야 합니다.")
        }

        // 4. 결제
        val paymentCommand = criteria.toPaymentCommand(totalPrice, couponDiscountPrice)
        val payment = paymentService.createPayment(paymentCommand)

        // 5. 주문 생성
        val orderCommand = criteria.toCreateOrderCommand(
            payment.paymentId,
            products.associateBy({it.productId},{it.price}),
            couponDiscountPrice
        )
        val order = orderService.createOrder(orderCommand)

        // 6. 주문 조회 및 결과 반환
        val orderItems = orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(order.orderId))
        val orderDiscounts = orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(order.orderId))
        
        return OrderResult.Single.from(order, orderItems, orderDiscounts)
    }
}
