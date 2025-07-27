package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.LogoutFilter
import org.springframework.security.web.csrf.*
import org.springframework.web.client.RestClient
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2UserPersistenceService: OAuth2UserPersistenceService,
    private val loggingFilter: LoggingFilter,
    private val spaCsrfTokenRequestHandler: SpaCsrfTokenRequestHandler
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http // ...
            .authorizeHttpRequests { requests ->
                requests.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                    .requestMatchers("/", "/error", "/webjars/**", "/me/following").permitAll().anyRequest()
                    .authenticated()
            }
            .addFilterBefore(loggingFilter, LogoutFilter::class.java)
            .logout { l ->
                l.logoutSuccessUrl("/").permitAll()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .csrf { c ->
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
            }
            .oauth2Login { o ->
                o.userInfoEndpoint { u ->
                    u.userService(oAuth2UserPersistenceService)
                }
                    .defaultSuccessUrl("http://localhost:3000", true)
            }
            .oauth2Client(Customizer.withDefaults())
            .cors { }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowCredentials = true
        configuration.allowedOrigins = listOf("http://localhost:3000")
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun restClient(authorizedClientManager: OAuth2AuthorizedClientManager): RestClient {
        val requestInterceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
        requestInterceptor.setClientRegistrationIdResolver(clientRegistrationIdResolver())

        return RestClient.builder().requestInterceptor(requestInterceptor).build()
    }

    private fun clientRegistrationIdResolver(): ClientRegistrationIdResolver {
        return ClientRegistrationIdResolver { request ->
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication is OAuth2AuthenticationToken) {
                authentication.authorizedClientRegistrationId
            } else {
                null
            }
        }
    }

}

