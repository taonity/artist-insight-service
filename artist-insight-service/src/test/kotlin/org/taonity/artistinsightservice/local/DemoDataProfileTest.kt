package org.taonity.artistinsightservice.local

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
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
    @Autowired lateinit var mapper: ObjectMapper

    @Test
    fun `demo user profile includes its portrait after login`() {
        val session = authorizeOAuth2("stub-user")
        val avatar = DemoCatalog.people[2].avatar!!

        mockMvc.perform(get("/user").cookie(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.privateUserObject.images[0].url").value(avatar))
        assertThat(users.findById("stub-user").orElseThrow().avatarUrl).isEqualTo(avatar)
    }

    @Test
    fun `demo owner signs in with a portrait and owner access`() {
        val session = authorizeOAuth2("stub-owner")
        val avatar = requireNotNull(DemoCatalog.people[0].avatar)

        mockMvc.perform(get("/user").cookie(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.privateUserObject.images[0].url").value(avatar))
        mockMvc.perform(get("/console/access/me").cookie(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.role").value("OWNER"))
        assertThat(users.findById("stub-owner").orElseThrow().avatarUrl).isEqualTo(avatar)
    }

    @Test
    fun `followings show Spotify genres, enriched genres, and enrichment availability`() {
        val session = authorizeOAuth2("stub-user")

        val response = mockMvc.perform(get("/followings").cookie(session))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.artists.length()").value(36))
            .andExpect(jsonPath("$.artists[0].artistObject.genres[0]").value("indie pop"))
            .andExpect(jsonPath("$.artists[0].genreEnriched").value(false))
            .andExpect(jsonPath("$.artists[1].artistObject.genres.length()").value(2))
            .andExpect(jsonPath("$.artists[1].genreEnriched").value(true))
            .andExpect(jsonPath("$.artists[2].artistObject.genres").isEmpty)
            .andExpect(jsonPath("$.artists[2].genreEnriched").value(false))
            .andExpect(jsonPath("$.advisories[0].code").value("GPT_ENRICHMENT_AVAILABLE"))
            .andExpect(jsonPath("$.advisories[0].detail").value(
                "You can enrich 5 followings with genres using GPT completion for free!"
            ))
            .andReturn().response
        val artists = mapper.readTree(response.contentAsByteArray).path("artists")
        assertThat(artists.count { row ->
            val artist = row.path("artistObject")
            artist.path("name").asText().length >= 48 || artist.path("genres").size() >= 6
        }).isEqualTo(7)
    }

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
            .isEqualTo(7)
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
        assertThat(users.findById("stub-user").orElseThrow().avatarUrl)
            .isEqualTo(DemoCatalog.people[2].avatar)
    }
}
