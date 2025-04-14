package kr.hhplus.be.server.domain.couponevent

import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Repository

interface CouponEventRepository {
    fun create(couponEvent: CouponEvent): CouponEvent
    fun findById(id: String): CouponEvent?
    fun findAll(): List<CouponEvent>
    fun decreaseStock(id: String): CouponEvent?
}