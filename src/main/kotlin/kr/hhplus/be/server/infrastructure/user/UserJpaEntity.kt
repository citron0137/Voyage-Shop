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
data class UserJpaEntity(
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
    /**
     * 엔티티 객체로부터 도메인 객체를 생성
     */
    fun toDomain(): User {
        return User(
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
    
    companion object {
        /**
         * 도메인 객체로부터 엔티티 객체를 생성
         */
        fun fromDomain(domain: User): UserJpaEntity {
            return UserJpaEntity(
                userId = domain.userId,
                createdAt = domain.createdAt,
                updatedAt = domain.updatedAt
            )
        }
    }
} 