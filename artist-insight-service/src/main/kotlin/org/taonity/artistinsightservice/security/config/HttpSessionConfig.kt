package org.taonity.artistinsightservice.security.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.session.web.http.DefaultCookieSerializer
import org.taonity.artistinsightservice.common.config.AppProperties

@Configuration
class HttpSessionConfig(private val properties: AppProperties) {
    @Bean
    fun authorizedClientService(jdbc: JdbcTemplate, registrations: ClientRegistrationRepository) =
        JdbcOAuth2AuthorizedClientService(jdbc, registrations)

    @Bean
    fun cookieSerializer(
        @Value("\${server.servlet.session.cookie.name}") name: String,
        @Value("\${server.servlet.session.cookie.domain}") domain: String,
    ) =
        DefaultCookieSerializer().apply {
            setCookieName(name)
            if (domain.isNotBlank()) setDomainName(domain)
            setCookiePath("/")
            setUseSecureCookie(properties.cookie.secure)
            setUseHttpOnlyCookie(true)
            setSameSite(properties.cookie.sameSite)
            // Browser-session cookie; inactivity timeout is enforced by the session repository.
        }
}
