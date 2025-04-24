package kr.hhplus.be.server.domain.user

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import java.time.LocalDateTime
import java.util.UUID

@DisplayName("사용자 서비스 테스트")
class UserServiceTest {
    private val userRepository = mockk<UserRepository>()
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        userService = UserService(userRepository)
    }

    @Nested
    @DisplayName("사용자 생성 시")
    inner class UserCreation {
        
        @Test
        @DisplayName("UUID가 생성되고 저장되어야 한다")
        fun creation_generatesUuidAndSaves() {
            // given
            every { userRepository.create(any()) } answers { firstArg() }

            // when
            val actualUser = userService.createUser(UserCommand.Create)

            // then
            verify { userRepository.create(any()) }
            assert(UUID.fromString(actualUser.userId) != null) // UUID 형식 검증
            assert(actualUser.createdAt != null)
            assert(actualUser.updatedAt != null)
        }
    }

    @Nested
    @DisplayName("ID로 사용자 조회 시")
    inner class UserRetrieval {
        
        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자 정보가 반환되어야 한다")
        fun withExistingId_returnsUser() {
            // given
            val userId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val expectedUser = User(userId, now, now)
            every { userRepository.findById(userId) } returns expectedUser

            // when
            val actualUser = userService.findUserById(UserQuery.GetById(userId))

            // then
            verify { userRepository.findById(userId) }
            assertEquals(expectedUser, actualUser)
        }
        
        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 null을 반환해야 한다")
        fun withNonExistentId_returnsNull() {
            // given
            val userId = UUID.randomUUID().toString()
            every { userRepository.findById(userId) } returns null

            // when
            val actualUser = userService.findUserById(UserQuery.GetById(userId))

            // then
            verify { userRepository.findById(userId) }
            assertNull(actualUser)
        }
    }
    
    @Nested
    @DisplayName("ID로 사용자 조회 또는 예외 발생 시")
    inner class UserRetrievalOrException {
        
        @Test
        @DisplayName("존재하는 ID로 조회하면 사용자 정보가 반환되어야 한다")
        fun withExistingId_returnsUser() {
            // given
            val userId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val expectedUser = User(userId, now, now)
            every { userRepository.findById(userId) } returns expectedUser

            // when
            val actualUser = userService.getUserById(UserQuery.GetById(userId))

            // then
            verify { userRepository.findById(userId) }
            assertEquals(expectedUser, actualUser)
        }
        
        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 NotFound 예외가 발생해야 한다")
        fun withNonExistentId_throwsNotFoundException() {
            // given
            val userId = UUID.randomUUID().toString()
            every { userRepository.findById(userId) } returns null

            // when & then
            val exception = assertThrows(UserException.NotFound::class.java) {
                userService.getUserById(UserQuery.GetById(userId))
            }
            
            verify { userRepository.findById(userId) }
        }
    }

    @Nested
    @DisplayName("모든 사용자 조회 시")
    inner class AllUsersRetrieval {
        
        @Test
        @DisplayName("모든 사용자 목록이 반환되어야 한다")
        fun retrieval_returnsAllUsers() {
            // given
            val now = LocalDateTime.now()
            val users = listOf(
                User(UUID.randomUUID().toString(), now, now),
                User(UUID.randomUUID().toString(), now, now)
            )
            every { userRepository.findAll() } returns users

            // when
            val actualUsers = userService.getAllUsers()

            // then
            verify { userRepository.findAll() }
            assertEquals(users, actualUsers)
        }
    }
} 