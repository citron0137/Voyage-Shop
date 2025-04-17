package kr.hhplus.be.server.domain.userpoint

import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service

interface UserPointRepository {
    // Create
    fun create(userPoint: UserPoint): UserPoint

    // Read
    fun findByUserId(userId: String): UserPoint?
    
    // Read with lock for concurrency control
    fun findByUserIdWithLock(userId: String): UserPoint?

    // Update
    fun save(userPoint: UserPoint): UserPoint
}