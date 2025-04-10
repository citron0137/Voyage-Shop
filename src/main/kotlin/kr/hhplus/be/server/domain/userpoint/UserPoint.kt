package kr.hhplus.be.server.domain.userpoint

import java.time.LocalDateTime

data class UserPoint (
    val userPointId: String,
    val userId: String,
    var amount: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
)