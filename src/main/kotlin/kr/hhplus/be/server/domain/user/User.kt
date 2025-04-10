package kr.hhplus.be.server.domain.user

import java.time.LocalDateTime

data class User(
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)