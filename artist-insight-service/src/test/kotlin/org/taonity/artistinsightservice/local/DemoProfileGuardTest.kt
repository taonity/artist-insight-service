package org.taonity.artistinsightservice.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

class DemoProfileGuardTest {
    @Test
    fun `demo fixtures and dev logins require local stub profiles and reject deployed environments`() {
        for ((profiles, enabled) in
            listOf(
                listOf("local", "stub-spotify", "demo-data") to true,
                listOf("stub-spotify", "demo-data") to false,
                listOf("local", "stub-spotify", "demo-data", "prod") to false,
                listOf("local", "stub-spotify", "demo-data", "stage") to false,
            )) {
            ApplicationContextRunner()
                .withInitializer { it.environment.setActiveProfiles(*profiles.toTypedArray()) }
                .withUserConfiguration(
                    DemoDataSeeder::class.java,
                    DemoSpotifyData::class.java,
                    DevLoginController::class.java,
                )
                .withBean(WireMockServer::class.java, { mock(WireMockServer::class.java) })
                .withBean(ObjectMapper::class.java, { ObjectMapper() })
                .withBean(
                    SpotifyUserRepository::class.java,
                    { mock(SpotifyUserRepository::class.java) },
                )
                .withBean(ArtistRepository::class.java, { mock(ArtistRepository::class.java) })
                .withBean(
                    DevAccessRepository::class.java,
                    { mock(DevAccessRepository::class.java) },
                )
                .withBean(AuditLogRepository::class.java, { mock(AuditLogRepository::class.java) })
                .run { context ->
                    assertThat(context.containsBean("demoDataSeeder")).isEqualTo(enabled)
                    assertThat(context.containsBean("demoSpotifyData")).isEqualTo(enabled)
                    assertThat(context.containsBean("devLoginController")).isEqualTo(enabled)
                }
        }
    }
}
