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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.*


@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2UserPersistenceService: OAuth2UserPersistenceService,
    private val spaCsrfTokenRequestHandler: SpaCsrfTokenRequestHandler,
    @Value("\${app.default-success-url}") private val defaultSuccessUrl: String,
    @Value("\${app.cors-allowed-origins}") private val corsAllowedOrigins: String
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
            .logout { l ->
                l.logoutSuccessUrl("/").permitAll()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .csrf { c ->
                c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
                    .ignoringRequestMatchers("/callback/kofi")
            }
            .oauth2Login { o ->
                o.userInfoEndpoint { u ->
                    u.userService(oAuth2UserPersistenceService)
                }
                    .defaultSuccessUrl(defaultSuccessUrl, true)
            }
            .oauth2Client(Customizer.withDefaults())
            .cors { }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): UrlBasedCorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowCredentials = true
        configuration.allowedOrigins = listOf(corsAllowedOrigins)
        configuration.allowedMethods = listOf("*")
        configuration.allowedHeaders = listOf("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}

