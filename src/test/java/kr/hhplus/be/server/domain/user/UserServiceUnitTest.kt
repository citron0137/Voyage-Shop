package kr.hhplus.be.server.domain.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.regex.Pattern

class UserServiceUnitTest {
    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        userService = UserService(userRepository)
    }

    @Test
    fun `사용자를 생성하면 UUID가 생성되고 저장된다`() {
        // given
        every { userRepository.create(any()) } answers { firstArg() }

        // when
        val actualUser = userService.createUser()

        // then
        verify { userRepository.create(any()) }
        assert(UUID.fromString(actualUser.userId) != null) // UUID 형식 검증
    }

    @Test
    fun `ID로 사용자를 조회할 수 있다`() {
        // given
        val userId = UUID.randomUUID().toString()
        val expectedUser = User(userId)
        every { userRepository.findById(userId) } returns expectedUser

        // when
        val actualUser = userService.findUserById(userId)

        // then
        verify { userRepository.findById(userId) }
        assertEquals(expectedUser, actualUser)
    }

    @Test
    fun `존재하지 않는 ID로 조회하면 null을 반환한다`() {
        // given
        val userId = UUID.randomUUID().toString()
        every { userRepository.findById(userId) } returns null

        // when
        val actualUser = userService.findUserById(userId)

        // then
        verify { userRepository.findById(userId) }
        assertNull(actualUser)
    }

    @Test
    fun `모든 사용자를 조회할 수 있다`() {
        // given
        val users = listOf(
            User(UUID.randomUUID().toString()),
            User(UUID.randomUUID().toString())
        )
        every { userRepository.findAll() } returns users

        // when
        val actualUsers = userService.getAllUsers()

        // then
        verify { userRepository.findAll() }
        assertEquals(users, actualUsers)
    }
}
