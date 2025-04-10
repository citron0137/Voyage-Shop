package kr.hhplus.be.server.domain.userpoint

data class UserPoint (
    val userPointId: String,
    val userId: String,
    var amount: Long,
)