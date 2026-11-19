package org.taonity.artistinsightservice.security

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.support.JdbcTransactionManager
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.oauth2.client.JdbcOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.session.SessionRepository
import org.springframework.session.jdbc.JdbcIndexedSessionRepository
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.support.TransactionTemplate
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal

@Sql("classpath:sql/clear-data.sql")
class PersistentSessionTest : ControllerTestsBaseClass() {
    @Autowired lateinit var jdbc: JdbcTemplate
    @Autowired lateinit var registrations: ClientRegistrationRepository
    @Autowired lateinit var sessions: JdbcIndexedSessionRepository

    @Test
    fun `session and Spotify credentials reload from fresh repository instances`() {
        val cookie = authorizeOAuth2()
        mockMvc.perform(get("/user").cookie(cookie)).andExpect(status().isOk)
        val id =
            jdbc.queryForObject(
                "select session_id from spring_session where principal_name = ?",
                String::class.java,
                "3126nx54y24ryqyza3qxcchi4wry",
            )!!
        val fresh =
            JdbcIndexedSessionRepository(
                jdbc,
                TransactionTemplate(JdbcTransactionManager(jdbc.dataSource!!)),
            )
        try {
            val context =
                (fresh as SessionRepository<*>)
                    .findById(id)!!
                    .getAttribute<SecurityContext>(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
                    )
            val principal = context.authentication!!.principal as SpotifyUserPrincipal
            assertThat(principal.name).isEqualTo(principal.getSpotifyId())
            assertThat(principal.privateUserObject.images.first().url).startsWith("https://")
            val client =
                JdbcOAuth2AuthorizedClientService(jdbc, registrations)
                    .loadAuthorizedClient<OAuth2AuthorizedClient>(
                        "spotify-artist-insight-service",
                        principal.name,
                    )
            assertThat(client.refreshToken).isNotNull()
            assertThat(client.accessToken.tokenValue).isNotBlank()
            val service = JdbcOAuth2AuthorizedClientService(jdbc, registrations)
            val now = Instant.now()
            val expired =
                OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    "expired-test-token",
                    now.minusSeconds(7200),
                    now.minusSeconds(3600),
                    client.accessToken.scopes,
                )
            service.saveAuthorizedClient(
                OAuth2AuthorizedClient(
                    client.clientRegistration,
                    principal.name,
                    expired,
                    client.refreshToken,
                ),
                context.authentication,
            )
            val manager =
                org.springframework.security.oauth2.client
                    .AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, service)
            manager.setAuthorizedClientProvider(
                OAuth2AuthorizedClientProviderBuilder.builder().refreshToken().build()
            )
            val refreshed =
                manager.authorize(
                    OAuth2AuthorizeRequest.withClientRegistrationId(
                            "spotify-artist-insight-service"
                        )
                        .principal(context.authentication)
                        .build()
                )!!
            assertThat(refreshed.accessToken.expiresAt).isAfter(now)
            assertThat(
                    JdbcOAuth2AuthorizedClientService(jdbc, registrations)
                        .loadAuthorizedClient<OAuth2AuthorizedClient>(
                            "spotify-artist-insight-service",
                            principal.name,
                        )
                        .accessToken
                        .tokenValue
                )
                .isEqualTo(refreshed.accessToken.tokenValue)
        } finally {
            fresh.destroy()
        }
        mockMvc.perform(get("/followings").cookie(cookie)).andExpect(status().isOk)
        mockMvc.perform(post("/logout").cookie(cookie).with(csrf())).andExpect(status().isOk)
        assertThat((sessions as SessionRepository<*>).findById(id)).isNull()
        assertThat(
                jdbc.queryForObject(
                    "select count(*) from oauth2_authorized_client where principal_name = ?",
                    Int::class.java,
                    "3126nx54y24ryqyza3qxcchi4wry",
                )
            )
            .isZero()
        mockMvc.perform(get("/user").cookie(cookie)).andExpect(status().isUnauthorized)
    }
}
