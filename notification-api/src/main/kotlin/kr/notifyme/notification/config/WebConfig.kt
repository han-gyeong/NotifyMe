package kr.notifyme.notification.config

import kr.notifyme.notification.support.auth.CurrentUserIdResolver
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Profile("!test")
@Configuration
class WebConfig(
    private val currentUserIdResolver: CurrentUserIdResolver
) : WebMvcConfigurer {

    override fun addArgumentResolvers(argumentResolvers: MutableList<HandlerMethodArgumentResolver>) {
        argumentResolvers.add(currentUserIdResolver)
    }

}