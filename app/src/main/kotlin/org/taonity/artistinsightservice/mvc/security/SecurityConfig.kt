package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.*
import org.springframework.web.client.RestClient

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2UserPersistenceService: OAuth2UserPersistenceService
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
            .logout { l ->
                l.logoutSuccessUrl("/").permitAll()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint { request, response, authException ->
                    response.sendRedirect("/")
                }
            }
            .csrf { c ->
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(SpaCsrfTokenRequestHandler())
            }
            .oauth2Login { o ->
                o.userInfoEndpoint { u ->
                    u.userService(oAuth2UserPersistenceService)
                }
            }
            .oauth2Client(Customizer.withDefaults())

        return http.build()
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

