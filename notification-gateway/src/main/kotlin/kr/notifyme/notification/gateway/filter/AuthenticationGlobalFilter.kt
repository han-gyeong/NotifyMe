package kr.notifyme.notification.gateway.filter

import kr.notifyme.notification.gateway.auth.TokenHandler
import kr.notifyme.notification.gateway.config.AuthWhiteListProperties
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class AuthenticationGlobalFilter(
    private val whiteListProperties: AuthWhiteListProperties,
    private val tokenHandler: TokenHandler,
) : GlobalFilter {

    private val pathMatcher = AntPathMatcher()

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.uri.path

        val isWhiteList = whiteListProperties.paths.any { pattern -> pathMatcher.match(pattern, path) }
        if (isWhiteList) {
            return chain.filter(exchange)
        }

        val authHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unAuthorizedResponse(exchange)
        }

        val token = authHeader.substring(7)

        try {
            val claims = tokenHandler.parseToken(token)

            val userInfo = tokenHandler.getUserInfo(claims)

            val request = exchange.request.mutate()
                .header("X-USER-ID", userInfo)
                .headers { it.remove(HttpHeaders.AUTHORIZATION) }
                .build()

            return chain.filter(exchange.mutate().request(request).build())
        } catch (e: Exception) {
            return unAuthorizedResponse(exchange)
        }
    }

    private fun unAuthorizedResponse(exchange: ServerWebExchange): Mono<Void> {
        val response = exchange.response
        response.statusCode = HttpStatus.UNAUTHORIZED
        return response.setComplete()
    }
}