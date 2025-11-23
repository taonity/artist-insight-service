package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.DispatcherType
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.csrf.*
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2UserPersistenceService: OAuth2UserPersistenceService,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler,
    private val spaCsrfTokenRequestHandler: SpaCsrfTokenRequestHandler,
    @Value("\${app.default-success-url}") private val defaultSuccessUrl: String,
    @Value("\${app.csrf-cookie-name}") private val csrfCookieName: String
) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http // ...
            .authorizeHttpRequests { requests ->
                requests.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                    .requestMatchers(
                        "/callback/kofi",
                        "/actuator/**",
                        //TODO: add for local only?
                        "/N4N11KVW3E",
                        "/"
                    ).permitAll().anyRequest()
                    .authenticated()
            }
            // TODO: do I need this?
            .logout { l ->
                l.logoutSuccessHandler { _, response, _ ->
                    response.status = 200
                }.permitAll()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .csrf { c ->
                val csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()
                csrfTokenRepository.setCookieName(csrfCookieName)
                c.csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
                    .ignoringRequestMatchers("/callback/kofi")
            }
            .oauth2Login { o ->
                o.userInfoEndpoint { u ->
                    u.userService(oAuth2UserPersistenceService)
                }
                    .defaultSuccessUrl(defaultSuccessUrl, true)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .oauth2Client(Customizer.withDefaults())

        return http.build()
    }
}

