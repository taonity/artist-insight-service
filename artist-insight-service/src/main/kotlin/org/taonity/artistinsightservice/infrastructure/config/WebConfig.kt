package org.taonity.artistinsightservice.infrastructure.config

import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.taonity.artistinsightservice.devaccess.interceptor.RateLimitInterceptor

@Configuration
class WebConfig(
    private val rateLimitInterceptor: ObjectProvider<RateLimitInterceptor>
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        rateLimitInterceptor.ifAvailable { registry.addInterceptor(it) }
    }
}