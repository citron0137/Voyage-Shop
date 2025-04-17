package kr.hhplus.be.server.integration.user

import kr.hhplus.be.server.TestcontainersConfiguration
import kr.hhplus.be.server.application.user.UserCriteria
import kr.hhplus.be.server.application.user.UserFacade
import kr.hhplus.be.server.domain.user.UserException
import kr.hhplus.be.server.domain.userpoint.UserPointService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Import(TestcontainersConfiguration::class)
@Transactional
class UserRegistrationFlowIntegrationTest {

    @Autowired
    private lateinit var userFacade: UserFacade
    
    @Autowired
    private lateinit var userPointService: UserPointService

    @Test
    @DisplayName("사용자 등록 시 사용자와 포인트 정보가 함께 생성된다")
    fun userRegistrationCreatesUserAndPoint() {
        // when: 사용자 생성
        val user = userFacade.createUser()
        
        // then: 사용자 정보 검증
        assertNotNull(user)
        assertNotNull(user.userId)
        
        // and then: 사용자 포인트 정보 검증
        val userPoint = userPointService.findByUserId(user.userId)
        assertNotNull(userPoint)
        assertEquals(user.userId, userPoint?.userId)
        assertEquals(0L, userPoint?.amount)
    }
    
    @Test
    @DisplayName("등록된 사용자는 ID로 조회할 수 있다")
    fun registeredUserCanBeFoundById() {
        // given: 등록된 사용자
        val createdUser = userFacade.createUser()
        
        // when: ID로 사용자 조회
        val foundUser = userFacade.findUserById(UserCriteria.GetById(createdUser.userId))
        
        // then: 조회된 사용자 정보 검증
        assertEquals(createdUser.userId, foundUser.userId)
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회하면 예외가 발생한다")
    fun findingNonExistentUserThrowsException() {
        // given: 존재하지 않는 사용자 ID
        val nonExistentUserId = UUID.randomUUID().toString()
        
        // when & then: 예외 발생 확인
        assertThrows<UserException.NotFound> {
            userFacade.findUserById(UserCriteria.GetById(nonExistentUserId))
        }
    }
    
    @Test
    @DisplayName("등록된 사용자는 전체 목록에서 조회할 수 있다")
    fun registeredUsersCanBeFoundInList() {
        // given: 여러 사용자 등록
        val user1 = userFacade.createUser()
        val user2 = userFacade.createUser()
        
        // when: 모든 사용자 조회
        val allUsers = userFacade.getAllUsers()
        
        // then: 등록한 사용자들이 목록에 포함되어 있는지 확인
        assertTrue(allUsers.users.any { it.userId == user1.userId })
        assertTrue(allUsers.users.any { it.userId == user2.userId })
    }
} 