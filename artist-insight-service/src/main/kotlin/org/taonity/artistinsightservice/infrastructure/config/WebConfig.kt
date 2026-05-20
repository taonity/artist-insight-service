package org.taonity.artistinsightservice.infrastructure.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.taonity.artistinsightservice.devaccess.interceptor.RateLimitInterceptor
import org.taonity.artistinsightservice.infrastructure.logging.ControllerLoggingInterceptor

@Configuration
class WebConfig(
    private val rateLimitInterceptor: ObjectProvider<RateLimitInterceptor>,
    private val controllerLoggingInterceptor: ControllerLoggingInterceptor
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(controllerLoggingInterceptor)
        rateLimitInterceptor.ifAvailable { registry.addInterceptor(it) }
    }
}