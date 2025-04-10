package kr.hhplus.be.server.controller.user.response

import java.time.LocalDateTime

class UserResponseDTO(
    val id: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)