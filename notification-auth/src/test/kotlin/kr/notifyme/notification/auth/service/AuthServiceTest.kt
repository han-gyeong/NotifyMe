package kr.notifyme.notification.auth.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.auth.controller.v1.request.SignUpRequest
import kr.notifyme.notification.auth.entity.User
import kr.notifyme.notification.auth.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.Test

class AuthServiceTest {

    private val tokenHandler: TokenHandler = mockk()
    private val tokenCacheService: TokenCacheService = mockk()
    private val userRepository: UserRepository = mockk()
    private val passwordEncoder: PasswordEncoder = mockk()

    private val authService: AuthService = AuthService(
        tokenHandler,
        tokenCacheService,
        userRepository,
        passwordEncoder
    )

    @Test
    fun `아이디와 이메일이 중복되지 않으면 회원가입에 성공한다`() = runTest {
        // given
        val request = SignUpRequest(userId = "test1", password = "pwtest1", email = "test@test.com")
        val user = User(1L, "test1", "test@test.com", "encodedPassword")

        coEvery { userRepository.existsByUserId(any()) } returns false
        coEvery { userRepository.existsByEmail(any()) } returns false
        coEvery { userRepository.save(any()) } returns user
        every { passwordEncoder.encode(any()) } returns "encodedPassword"

        // when
        val savedUser = authService.signUp(request)

        // then
        assertAll(
            { assertEquals(request.userId, savedUser.userId) },
            { assertEquals(request.email, savedUser.email) },
            { assertEquals("encodedPassword", savedUser.password) }

        )

        coVerify(exactly = 1) { userRepository.save(any()) }
    }

    @Test
    fun `아이디가 중복되었다면 회원가입은 실패한다`() = runTest {
        // given
        val request = SignUpRequest(userId = "test1", password = "pwtest1", email = "test@test.com")

        coEvery { userRepository.existsByUserId(request.userId) } returns true

        // when & then
        val exception = assertThrows<RuntimeException> {
            authService.signUp(request)
        }

        assertEquals(exception.message, "User already exists")
        coVerify(exactly = 0) { userRepository.save(any()) }
    }

    @Test
    fun `이메일이 중복되었다면 회원가입은 실패한다`() = runTest {
        // given
        val request = SignUpRequest(userId = "test1", password = "pwtest1", email = "test@test.com")

        coEvery { userRepository.existsByUserId(request.userId) } returns false
        coEvery { userRepository.existsByEmail(request.email) } returns true

        // when & then
        val exception = assertThrows<RuntimeException> {
            authService.signUp(request)
        }

        assertEquals(exception.message, "Email already exists")
        coVerify(exactly = 0) { userRepository.save(any()) }
    }




}