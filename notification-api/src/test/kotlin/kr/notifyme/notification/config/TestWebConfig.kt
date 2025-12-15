package kr.notifyme.notification.config

import kr.notifyme.notification.service.NotificationService
import kr.notifyme.notification.support.auth.TestCurrentUserIdResolver
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Profile("test")
@TestConfiguration
class TestWebConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(TestCurrentUserIdResolver())
    }

    @Bean
    fun notificationService(): NotificationService = Mockito.mock(NotificationService::class.java)
}