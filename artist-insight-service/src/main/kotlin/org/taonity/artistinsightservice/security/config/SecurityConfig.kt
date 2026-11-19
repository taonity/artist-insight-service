package org.taonity.artistinsightservice.security.config

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
import org.springframework.security.web.context.SecurityContextHolderFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.taonity.artistinsightservice.security.handler.OAuth2AuthenticationFailureHandler
import org.taonity.artistinsightservice.security.filter.UserMdcFilter
import org.taonity.artistinsightservice.security.service.OAuth2UserPersistenceService
import org.taonity.artistinsightservice.security.handler.SpaCsrfTokenRequestHandler

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val oAuth2UserPersistenceService: OAuth2UserPersistenceService,
    private val oAuth2AuthenticationFailureHandler: OAuth2AuthenticationFailureHandler,
    private val spaCsrfTokenRequestHandler: SpaCsrfTokenRequestHandler,
    private val properties: org.taonity.artistinsightservice.common.config.AppProperties,
    private val authorizedClients: org.springframework.security.oauth2.client.OAuth2AuthorizedClientService,
    private val userMdcFilter: UserMdcFilter,

) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .addFilterAfter(userMdcFilter, SecurityContextHolderFilter::class.java)
            .authorizeHttpRequests { requests ->
                requests.dispatcherTypeMatchers(DispatcherType.FORWARD).permitAll()
                    .requestMatchers(
                        "/dev/stub-users",
                        "/callback/kofi",
                        "/actuator/**",
                        "/development-access-request",
                        //TODO: add for local only?
                        "/N4N11KVW3E",
                        "/",
                        "/share/{shareCode}"
                    ).permitAll().anyRequest()
                    .authenticated()
            }
            // TODO: do I need this?
            .logout { l ->
                l.addLogoutHandler { _, _, authentication ->
                    if (authentication is org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken) {
                        authorizedClients.removeAuthorizedClient(authentication.authorizedClientRegistrationId, authentication.name)
                    }
                }
                l.logoutSuccessHandler { _, response, _ ->
                    response.status = 200
                }.permitAll()
            }
            .exceptionHandling { e ->
                e.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .csrf { c ->
                val csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse()
                csrfTokenRepository.setCookieName(properties.csrfCookieName)
                csrfTokenRepository.setCookieCustomizer { builder ->
                    builder.secure(properties.cookie.secure).sameSite(properties.cookie.sameSite)
                }
                c.csrfTokenRepository(csrfTokenRepository)
                    .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
                    .ignoringRequestMatchers("/callback/kofi")
            }
            .oauth2Login { o ->
                o.userInfoEndpoint { u ->
                    u.userService(oAuth2UserPersistenceService)
                }
                    .defaultSuccessUrl(properties.defaultSuccessUrl, true)
                    .failureHandler(oAuth2AuthenticationFailureHandler)
            }
            .oauth2Client(Customizer.withDefaults())

        return http.build()
    }
}