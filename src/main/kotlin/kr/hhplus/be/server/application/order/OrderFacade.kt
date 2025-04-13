package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.coupon.CouponUserCommand
import kr.hhplus.be.server.domain.coupon.CouponUserService
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.payment.PaymentCommand
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.ProductCommand
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.user.UserService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 주문 파사드
 * 주문 관련 비즈니스 로직을 캡슐화하고 컨트롤러에서 사용할 수 있는 단순한 인터페이스를 제공합니다.
 */
@Component
class OrderFacade(
    private val orderService: OrderService,
    private val userService: UserService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
    private val couponUserService: CouponUserService
) {
    /**
     * 주문 ID로 주문 정보를 조회합니다.
     *
     * @param orderId 조회할 주문 ID
     * @return 주문 정보
     * @throws OrderException.OrderIdShouldNotBlank 주문 ID가 빈 값인 경우
     * @throws OrderException.NotFound 주문을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getOrder(orderId: String): OrderResult {
        if (orderId.isBlank()) {
            throw OrderException.OrderIdShouldNotBlank("주문 ID는 비어있을 수 없습니다.")
        }
        
        val command = OrderCommand.GetById(orderId)
        val order = orderService.getOrderById(command)
        
        // 주문 항목 조회
        val itemsCommand = OrderItemCommand.GetByOrderId(orderId)
        val items = orderService.getOrderItemsByOrderId(itemsCommand)
        
        // 주문 할인 조회
        val discountsCommand = OrderDiscountCommand.GetByOrderId(orderId)
        val discounts = orderService.getOrderDiscountsByOrderId(discountsCommand)
        
        return OrderResult.from(order, items, discounts)
    }
    
    /**
     * 사용자 ID로 주문 목록을 조회합니다.
     *
     * @param userId 조회할 사용자 ID
     * @return 주문 목록
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    fun getOrdersByUserId(userId: String): OrderListResult {
        if (userId.isBlank()) {
            throw UserException.UserIdShouldNotBlank("사용자 ID는 비어있을 수 없습니다.")
        }
        
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        val command = OrderCommand.GetByUserId(userId)
        val orders = orderService.getOrdersByUserId(command)
        
        // 주문이 없으면 빈 목록 반환
        if (orders.isEmpty()) {
            return OrderListResult(emptyList())
        }
        
        // 각 주문의 항목과 할인 정보 조회
        val orderIds = orders.map { it.orderId }
        val allItems = orderIds.flatMap { 
            orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId(it)) 
        }
        val allDiscounts = orderIds.flatMap { 
            orderService.getOrderDiscountsByOrderId(OrderDiscountCommand.GetByOrderId(it)) 
        }
        
        // 주문 ID별로 그룹화
        val itemsByOrderId = allItems.groupBy { it.orderId }
        val discountsByOrderId = allDiscounts.groupBy { it.orderId }
        
        return OrderListResult.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
    }
    
    /**
     * 모든 주문 목록을 조회합니다.
     *
     * @return 주문 목록
     */
    @Transactional(readOnly = true)
    fun getAllOrders(): OrderListResult {
        val orders = orderService.getAllOrders()
        
        // 주문이 없으면 빈 목록 반환
        if (orders.isEmpty()) {
            return OrderListResult(emptyList())
        }
        
        // 각 주문의 항목과 할인 정보 조회
        val orderIds = orders.map { it.orderId }
        val allItems = orderIds.flatMap { 
            orderService.getOrderItemsByOrderId(OrderItemCommand.GetByOrderId(it)) 
        }
        val allDiscounts = orderIds.flatMap { 
            orderService.getOrderDiscountsByOrderId(OrderDiscountCommand.GetByOrderId(it)) 
        }
        
        // 주문 ID별로 그룹화
        val itemsByOrderId = allItems.groupBy { it.orderId }
        val discountsByOrderId = allDiscounts.groupBy { it.orderId }
        
        return OrderListResult.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
    }
    
    /**
     * 새로운 주문을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param orderItems 주문 항목 목록
     * @param couponUserId 사용할 쿠폰 ID (optional)
     * @return 생성된 주문 정보
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     * @throws ProductException.NotFound 상품을 찾을 수 없는 경우
     * @throws ProductException.StockAmountUnderflow 상품 재고가 부족한 경우
     * @throws OrderException.OrderItemRequired 주문 항목이 없는 경우
     * @throws OrderException.FinalAmountShouldMoreThan0 최종 결제 금액이 0 이하인 경우
     */
    @Transactional
    fun createOrder(
        userId: String,
        orderItems: List<OrderItemRequest>,
        couponUserId: String? = null
    ): OrderResult {
        // 사용자 존재 여부 확인
        userService.findUserByIdOrThrow(userId)
        
        if (orderItems.isEmpty()) {
            throw OrderException.OrderItemRequired("최소 1개 이상의 주문 상품이 필요합니다.")
        }
        
        // 주문 항목 생성 및 상품 재고 확인/감소
        val orderItemCommands = orderItems.map { item ->
            // 상품 존재 여부와 가격 확인
            val product = productService.getProduct(item.productId)
            
            // 재고 감소
            val decreaseCommand = ProductCommand.DecreaseStock(
                productId = item.productId,
                amount = item.amount
            )
            try {
                productService.decreaseStock(decreaseCommand)
            } catch (e: ProductException.StockAmountUnderflow) {
                throw ProductException.StockAmountUnderflow("상품 ${item.productId}의 재고가 부족합니다.")
            }
            
            // 주문 항목 명령 생성
            OrderItemCommand.Create(
                productId = item.productId,
                amount = item.amount,
                unitPrice = product.price
            )
        }
        
        // 총 주문 금액 계산
        val totalAmount = orderItemCommands.sumOf { it.totalPrice }
        
        // 할인 처리
        var totalDiscountAmount = 0L
        val orderDiscountCommands = mutableListOf<OrderDiscountCommand.Create>()
        
        if (couponUserId != null) {
            // 쿠폰 조회 및 할인 금액 계산
            val couponUser = couponUserService.getCouponUser(couponUserId)
            val discountAmount = couponUser.calculateDiscountAmount(totalAmount)
            
            if (discountAmount > 0) {
                // 쿠폰 사용 처리
                val useCommand = CouponUserCommand.Use(couponUserId)
                couponUserService.use(useCommand)
                
                // 할인 정보 추가
                totalDiscountAmount += discountAmount
                orderDiscountCommands.add(
                    OrderDiscountCommand.Create(
                        discountType = DiscountType.COUPON,
                        discountId = couponUserId,
                        discountAmount = discountAmount
                    )
                )
            }
        }
        
        // 결제 처리
        val finalAmount = totalAmount - totalDiscountAmount
        val paymentCommand = PaymentCommand.Create(
            userId = userId,
            totalPaymentAmount = finalAmount
        )
        val payment = paymentService.createPayment(paymentCommand)
        
        // 주문 생성
        val orderCommand = OrderCommand.Create(
            userId = userId,
            paymentId = payment.paymentId,
            orderItems = orderItemCommands,
            orderDiscounts = orderDiscountCommands
        )
        
        val order = orderService.createOrder(orderCommand)
        
        // 주문 항목 및 할인 조회
        val itemsCommand = OrderItemCommand.GetByOrderId(order.orderId)
        val items = orderService.getOrderItemsByOrderId(itemsCommand)
        
        val discountsCommand = OrderDiscountCommand.GetByOrderId(order.orderId)
        val discounts = orderService.getOrderDiscountsByOrderId(discountsCommand)
        
        return OrderResult.from(order, items, discounts)
    }
}

/**
 * 주문 항목 요청 클래스
 */
data class OrderItemRequest(
    val productId: String,
    val amount: Long
) {
    init {
        if (productId.isBlank()) throw OrderException.ProductIdShouldNotBlank("상품 ID는 비어있을 수 없습니다.")
        if (amount <= 0) throw OrderException.AmountShouldMoreThan0("주문 수량은 0보다 커야합니다.")
    }
} 