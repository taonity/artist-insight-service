package org.taonity.artistinsightservice.local

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@ActiveProfiles("local", "demo-data")
@DirtiesContext
class DemoDataProfileTest : ControllerTestsBaseClass() {
    @Autowired lateinit var seeder: DemoDataSeeder
    @Autowired lateinit var users: SpotifyUserRepository
    @Autowired lateinit var requests: DevAccessRepository
    @Autowired lateinit var audits: AuditLogRepository

    @Test
    fun `seeding is idempotent and preserves existing records`() {
        seeder.seed()
        val userCount = users.count()
        val requestCount = requests.count()
        val auditCount = audits.count()
        users.save(users.findById("stub-user").orElseThrow().apply { gptUsagesLeft = 37 })
        seeder.seed()
        assertThat(users.count()).isEqualTo(userCount)
        assertThat(requests.count()).isEqualTo(requestCount)
        assertThat(audits.count()).isEqualTo(auditCount)
        assertThat(users.findById("stub-user").orElseThrow().gptUsagesLeft).isEqualTo(37)
    }
}
