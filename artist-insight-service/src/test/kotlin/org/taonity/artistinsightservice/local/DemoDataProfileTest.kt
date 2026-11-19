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
        assertThat(userCount).isGreaterThanOrEqualTo(120)
        assertThat(requestCount).isGreaterThanOrEqualTo(96)
        assertThat(auditCount).isGreaterThanOrEqualTo(240)
        DemoCatalog.people.forEach { person ->
            val user = users.findById(person.id).orElseThrow()
            assertThat(user.displayName).isEqualTo(person.name)
            assertThat(user.avatarUrl).isEqualTo(person.avatar)
        }
        assertThat(DemoCatalog.people.count { it.avatar == null }).isGreaterThanOrEqualTo(20)
        assertThat(DemoCatalog.people.count { it.avatar?.contains("portrait-") == true })
            .isEqualTo(5)
        assertThat(DemoCatalog.people.mapNotNull { it.avatar }.distinct())
            .hasSizeGreaterThanOrEqualTo(11)
        assertThat(requests.findAll().map { it.status }.distinct()).hasSize(3)
        users.save(
            users.findById("stub-user").orElseThrow().apply {
                gptUsagesLeft = 37
                displayName = "Local user"
                avatarUrl = null
            }
        )
        seeder.seed()
        assertThat(users.count()).isEqualTo(userCount)
        assertThat(requests.count()).isEqualTo(requestCount)
        assertThat(audits.count()).isEqualTo(auditCount)
        assertThat(users.findById("stub-user").orElseThrow().gptUsagesLeft).isEqualTo(37)
        assertThat(users.findById("stub-user").orElseThrow().displayName).isEqualTo("Rumi Cade")
        assertThat(users.findById("stub-user").orElseThrow().avatarUrl).isNull()
    }
}
