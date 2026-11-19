package org.taonity.artistinsightservice.local

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.boot.test.context.runner.ApplicationContextRunner
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
                .withUserConfiguration(DemoDataSeeder::class.java, DevLoginController::class.java)
                .withBean(
                    SpotifyUserRepository::class.java,
                    { mock(SpotifyUserRepository::class.java) },
                )
                .withBean(
                    DevAccessRepository::class.java,
                    { mock(DevAccessRepository::class.java) },
                )
                .withBean(AuditLogRepository::class.java, { mock(AuditLogRepository::class.java) })
                .run { context ->
                    assertThat(context.containsBean("demoDataSeeder")).isEqualTo(enabled)
                    assertThat(context.containsBean("devLoginController")).isEqualTo(enabled)
                }
        }
    }
}
