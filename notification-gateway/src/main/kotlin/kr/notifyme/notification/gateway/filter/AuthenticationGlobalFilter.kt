package kr.notifyme.notification.gateway.filter

import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kr.notifyme.notification.gateway.auth.TokenCacheService
import kr.notifyme.notification.gateway.auth.TokenHandler
import kr.notifyme.notification.gateway.config.AuthWhiteListProperties
import org.slf4j.LoggerFactory
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationGlobalFilter(
    private val tokenCacheService: TokenCacheService,
    private val whiteListProperties: AuthWhiteListProperties,
    private val tokenHandler: TokenHandler,
) : GlobalFilter, Ordered {

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        private const val HEADER_USER_ID = "X-USER-ID"
        private val log = LoggerFactory.getLogger(AuthenticationGlobalFilter::class.java)
    }

    private val pathMatcher = AntPathMatcher()

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> = mono {
        val request = exchange.request
        val path = request.uri.path

        val isWhiteList = whiteListProperties.paths.any { pattern -> pathMatcher.match(pattern, path) }
        if (isWhiteList) {
            return@mono chain.filter(exchange).awaitSingleOrNull()
        }

        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return@mono unAuthorizedResponse(exchange).awaitSingleOrNull()
        }

        val token = authHeader.removePrefix(BEARER_PREFIX)

        try {
            val claims = tokenHandler.parseToken(token)
            val userId = tokenHandler.getUserId(claims)

            val isBlacklistToken = tokenCacheService.isTokenBlacklisted(token)
            if (isBlacklistToken) {
                return@mono unAuthorizedResponse(exchange).awaitSingleOrNull()
            }

            val mutatedRequest = exchange.request.mutate()
                .header(HEADER_USER_ID, userId)
                .headers { it.remove(HttpHeaders.AUTHORIZATION) }
                .build()

            return@mono chain.filter(exchange.mutate().request(mutatedRequest).build()).awaitSingleOrNull()
        } catch (e: Exception) {
            log.error("error on validation: {}", path, e)
            return@mono unAuthorizedResponse(exchange).awaitSingleOrNull()
        }
    }

    private fun unAuthorizedResponse(exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        return response.setComplete()
    }

    override fun getOrder(): Int {
        return -1
    }
}