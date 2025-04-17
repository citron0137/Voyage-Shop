package kr.hhplus.be.server.infrastructure.user

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.hhplus.be.server.domain.user.User
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * User 도메인을 위한 JPA 엔티티 클래스
 */
@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    val userId: String,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 도메인 객체를 엔티티로 변환
         */
        fun of(user: User): UserEntity {
            return UserEntity(
                userId = user.userId,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }

        /**
         * 엔티티를 도메인 객체로 변환
         */
        fun toDomain(entity: UserEntity): User {
            return User(
                userId = entity.userId,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt
            )
        }
    }
} 