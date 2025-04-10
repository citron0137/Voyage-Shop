package kr.hhplus.be.server.domain.userpoint

class UserPointCommand {

    class Create (
        val userId: String,
    )

    class Charge (
        val userId: String,
        val amount: Long,
    )

    class Use (
        val userId: String,
        val amount: Long,
    )

}