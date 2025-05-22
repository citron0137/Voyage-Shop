package kr.hhplus.be.server.infrastructure.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import jakarta.persistence.LockModeType

/**
 * ProductJpaEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface ProductJpaRepository : JpaRepository<ProductJpaEntity, String> {
    /**
     * ID로 상품 정보를 조회하면서 동시성 제어를 위한 락을 획득합니다.
     * 
     * @param id 상품 ID
     * @return 해당 상품 정보 (락 획득)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductJpaEntity p WHERE p.productId = :id")
    fun findByIdWithLock(id: String): ProductJpaEntity?
} 