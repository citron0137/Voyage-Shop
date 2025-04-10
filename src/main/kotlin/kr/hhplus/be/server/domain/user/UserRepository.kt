package kr.hhplus.be.server.domain.user

interface UserRepository {
    fun create(user:User):User
    fun findById(userId:String):User?
    fun findAll(): List<User>
}