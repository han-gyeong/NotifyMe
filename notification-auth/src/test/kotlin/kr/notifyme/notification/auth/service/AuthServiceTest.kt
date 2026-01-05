package kr.notifyme.notification.auth.service

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kr.notifyme.notification.auth.controller.v1.request.LoginRequest
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
    private val tokenCacheService: TokenCacheService = mockk(relaxed = true)
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

    @Test
    fun `아이디와 패스워드가 일치하면 로그인이 성공하고 토큰을 반환한다`() = runTest {
        // given
        val request = LoginRequest(userId = "test1", password = "pwtest1")
        val user = User(id = 1L, userId = "test1", password = "pwtest1", email = "test@test.com")

        coEvery { userRepository.findByUserId(request.userId) } returns user
        every { passwordEncoder.matches(any(), any()) } returns true
        every { tokenHandler.createAccessToken(user.userId) } returns "accessToken"
        every { tokenHandler.createRefreshToken(user.userId) } returns "refreshToken"

        // when
        val tokenResponse = authService.login(request)

        // then
        assertAll(
            { assertEquals(tokenResponse.accessToken, "accessToken") },
            { assertEquals(tokenResponse.refreshToken, "refreshToken") }
        )

        coVerify(exactly = 1) { tokenCacheService.saveRefreshToken(user.userId, tokenResponse.refreshToken) }
    }

    @Test
    fun `아이디가 존재하지 않을 경우 로그인은 실패한다`() = runTest {
        // given
        val request = LoginRequest(userId = "test1", password = "pwtest1")

        coEvery { userRepository.findByUserId(request.userId) } returns null

        // when & then
        val exception = assertThrows<RuntimeException> {
            authService.login(request)
        }

        assertEquals(exception.message, "Invalid ID or password")
    }

    @Test
    fun `이메일이 존재하지 않을 경우 로그인은 실패한다`() = runTest {
        // given
        val request = LoginRequest(userId = "test1", password = "pwtest1")
        val user = User(id = 1L, userId = "test1", password = "pwtest1", email = "test@test.com")

        coEvery { userRepository.findByUserId(request.userId) } returns user
        every { passwordEncoder.matches(any(), any()) } returns false

        // when & then
        val exception = assertThrows<RuntimeException> {
            authService.login(request)
        }

        assertEquals(exception.message, "Invalid ID or password")
    }

    @Test
    fun `정상적인 액세스 토큰으로 로그아웃을 할 수 있다`() = runTest {
        // given
        val accessToken = "accessToken"
        val claims = Jwts.claims().setSubject("test")

        every { tokenHandler.parseToken(accessToken) } returns claims
        every { tokenHandler.getRemainingExpiration(accessToken) } returns 1000

        // when
        authService.logout(accessToken)

        // then
        coVerify(exactly = 1) { tokenCacheService.removeRefreshToken("test") }
        coVerify(exactly = 1) { tokenCacheService.addBlacklist(accessToken, 1000) }
    }

    @Test
    fun `액세스 토큰이 잘못된 경우 로그아웃 시 예외가 발생한다`() = runTest {
        // given
        every { tokenHandler.parseToken(any()) } throws MalformedJwtException("ERROR")

        // when & then
        val exception = assertThrows<RuntimeException> {
            authService.logout("accessToken")
        }

        assertEquals(exception.message, "Invalid Token")
    }
}