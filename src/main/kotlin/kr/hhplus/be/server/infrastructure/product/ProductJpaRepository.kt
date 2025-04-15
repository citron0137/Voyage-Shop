package kr.hhplus.be.server.infrastructure.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * ProductEntity에 대한 Spring Data JPA 리포지토리 인터페이스
 */
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, String> 