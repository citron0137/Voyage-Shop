package kr.hhplus.be.server.application.order

import kr.hhplus.be.server.domain.couponuser.CouponUserCommand
import kr.hhplus.be.server.domain.couponuser.CouponUserService
import kr.hhplus.be.server.domain.order.*
import kr.hhplus.be.server.domain.payment.PaymentService
import kr.hhplus.be.server.domain.product.ProductException
import kr.hhplus.be.server.domain.product.ProductQuery
import kr.hhplus.be.server.domain.product.ProductService
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.shared.lock.DistributedLockManager
import kr.hhplus.be.server.shared.lock.LockKeyConstants
import kr.hhplus.be.server.shared.lock.LockKeyGenerator
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

/**
 * 주문 애플리케이션 서비스
 * 여러 도메인 서비스를 조합하여 주문 생성, 조회 등의 비즈니스 유스케이스를 구현하고 트랜잭션을 관리합니다.
 */
@Component
class OrderApplication(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val paymentService: PaymentService,
    private val couponUserService: CouponUserService,
    private val lockManager: DistributedLockManager,
    private val transactionManager: PlatformTransactionManager
) {

    /**
     * 주문 ID로 주문 정보를 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 정보
     * @throws OrderException.OrderIdShouldNotBlank 주문 ID가 빈 값인 경우
     * @throws OrderException.NotFound 주문을 찾을 수 없는 경우
     */
    fun getOrder(criteria: OrderCriteria.GetById): OrderResult.Single {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = true
        
        return transactionTemplate.execute {
            val order = orderService.getOrderById(criteria.toQuery())
            
            // 주문 항목 조회
            val items = orderService.getOrderItemsByOrderId(criteria.toItemsQuery())
            
            // 주문 할인 조회
            val discounts = orderService.getOrderDiscountsByOrderId(criteria.toDiscountsQuery())
            
            OrderResult.Single.from(order, items, discounts)
        }!!
    }
    
    /**
     * 사용자 ID로 주문 목록을 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 목록
     * @throws UserException.UserIdShouldNotBlank 사용자 ID가 빈 값인 경우
     * @throws UserException.NotFound 사용자를 찾을 수 없는 경우
     */
    fun getOrdersByUserId(criteria: OrderCriteria.GetByUserId): OrderResult.List {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = true
        
        return transactionTemplate.execute {
            val orders = orderService.getOrdersByUserId(criteria.toQuery())
            
            // 주문이 없으면 빈 목록 반환
            if (orders.isEmpty()) {
                return@execute OrderResult.List(emptyList())
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
            
            OrderResult.List.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
        }!!
    }
    

    /**
     * 모든 주문 목록을 조회합니다.
     *
     * @param criteria 주문 조회 기준
     * @return 주문 목록
     */
    fun getAllOrders(criteria: OrderCriteria.GetAll): OrderResult.List {
        val transactionTemplate = TransactionTemplate(transactionManager)
        transactionTemplate.isReadOnly = true
        
        return transactionTemplate.execute {
            val orders = orderService.getAllOrders(criteria.toQuery())
            
            // 주문이 없으면 빈 목록 반환
            if (orders.isEmpty()) {
                return@execute OrderResult.List(emptyList())
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
            
            OrderResult.List.fromWithDetails(orders, itemsByOrderId, discountsByOrderId)
        }!!
    }
    
    /**
     * 새로운 주문을 생성합니다.
     * 분산 락을 먼저 획득한 후 트랜잭션을 시작하여 동시성 문제를 효과적으로 방지합니다.
     * 여러 리소스(상품, 쿠폰, 사용자)에 대해 개별적으로 락을 획득합니다.
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
    fun createOrder(criteria: OrderCriteria.Create): OrderResult.Single {
        // 1. 먼저 모든 필요한 락을 획득 (사용자, 상품들, 쿠폰)
        val orderKey = LockKeyGenerator.Order.userLock(criteria.userId)
        
        // 상품 ID들 추출
        val productIds = criteria.items.map { it.productId }
        val productKeys = productIds.map { LockKeyGenerator.Product.stockLock(it) }
        
        // 쿠폰 키 (있는 경우)
        val couponKey = criteria.couponUserId?.let { LockKeyGenerator.CouponUser.idLock(it) }
        
        // 모든 락 키를 하나의 리스트로 (사용자 -> 상품들 -> 쿠폰 순서로 획득)
        val allKeys = listOf(orderKey) + lockManager.sortLockKeys(productKeys) + listOfNotNull(couponKey)
        
        // 각 락별 타임아웃 설정 (첫 번째 락은 30초, 나머지는 10초)
        val timeouts = listOf(LockKeyConstants.EXTENDED_TIMEOUT) + 
            List(allKeys.size - 1) { LockKeyConstants.DEFAULT_TIMEOUT }
        
        // 모든 락을 순서대로 획득
        return lockManager.withOrderedLocks(
            keys = allKeys,
            timeouts = timeouts,
            action = {
                // 모든 락을 획득한 후에 트랜잭션 시작
                val transactionTemplate = TransactionTemplate(transactionManager)
                transactionTemplate.execute {
                    // 재고 차감
                    val decreaseStockCommands = criteria.toDecreaseStockAmountCommands()
                    val products = decreaseStockCommands.map { command ->
                        productService.decreaseProductStock(command)
                        productService.getProductById(ProductQuery.GetById(command.productId))
                    }

                    // 총 금액 계산
                    val totalPrice = criteria.items.sumOf { item -> 
                        item.amount * products.find { it.productId == item.productId }!!.price 
                    }

                    // 쿠폰 적용 (있는 경우)
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

                    // 결제
                    val paymentCommand = criteria.toPaymentCommand(totalPrice, couponDiscountPrice)
                    val payment = paymentService.createPayment(paymentCommand)

                    // 주문 생성
                    val orderCommand = criteria.toCreateOrderCommand(
                        payment.paymentId,
                        products.associateBy({it.productId},{it.price}),
                        couponDiscountPrice
                    )
                    val order = orderService.createOrder(orderCommand)

                    // 주문 조회 및 결과 반환
                    val orderItems = orderService.getOrderItemsByOrderId(OrderQuery.GetOrderItemsByOrderId(order.orderId))
                    val orderDiscounts = orderService.getOrderDiscountsByOrderId(OrderQuery.GetOrderDiscountsByOrderId(order.orderId))
                    
                    OrderResult.Single.from(order, orderItems, orderDiscounts)
                }!!
            }
        )
    }
}
