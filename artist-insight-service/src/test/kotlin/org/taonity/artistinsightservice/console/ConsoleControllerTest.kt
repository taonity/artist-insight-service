package org.taonity.artistinsightservice.console

import java.time.Instant
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.taonity.artistinsightservice.console.entity.AuditLogEntity
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.settings.repository.ConfigOverrideRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Sql("classpath:sql/clear-data.sql")
class ConsoleControllerTest : ControllerTestsBaseClass() {
    @Autowired lateinit var users: SpotifyUserRepository
    @Autowired lateinit var audits: AuditLogRepository
    @Autowired lateinit var overrides: ConfigOverrideRepository
    private val actorId = "3126nx54y24ryqyza3qxcchi4wry"

    private fun grant(role: ConsoleRole) {
        users.save(users.findById(actorId).orElseThrow().apply { consoleRole = role })
    }

    @Test
    fun `ordinary users cannot inspect admin data and anonymous users must authenticate`() {
        mockMvc.perform(get("/console/users")).andExpect(status().isUnauthorized)
        val cookie = authorizeOAuth2()
        for (endpoint in listOf("users", "development-access-requests", "config", "audit-logs")) {
            mockMvc
                .perform(get("/console/$endpoint").cookie(cookie))
                .andExpect(status().isForbidden)
        }
        mockMvc
            .perform(get("/console/access/me").cookie(cookie))
            .andExpect(jsonPath("$.role").value("NONE"))
    }

    @Test
    fun `role changes require owner and cannot target self`() {
        val cookie = authorizeOAuth2()
        grant(ConsoleRole.ADMIN)
        users.save(SpotifyUserEntity("target", "Target", 7, ConsoleRole.OWNER))
        mockMvc
            .perform(
                put("/console/users/target/role")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"role":"NONE"}""")
            )
            .andExpect(status().isForbidden)
        grant(ConsoleRole.OWNER)
        mockMvc
            .perform(
                put("/console/users/$actorId/role")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"role":"NONE"}""")
            )
            .andExpect(status().isForbidden)
        mockMvc
            .perform(
                put("/console/users/target/role")
                    .cookie(cookie)
                    .contentType("application/json")
                    .content("""{"role":"ADMIN"}""")
            )
            .andExpect(status().isForbidden)
        mockMvc
            .perform(
                put("/console/users/target/role")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"role":"ADMIN"}""")
            )
            .andExpect(status().isOk)
        val entry = audits.findAll().single()
        assertThat(entry.actorSpotifyId).isEqualTo(actorId)
        assertThat(entry.beforeValue).isEqualTo("OWNER")
        assertThat(entry.afterValue).isEqualTo("ADMIN")
        assertThat(users.findById("target").orElseThrow().gptUsagesLeft).isEqualTo(7)
    }

    @Test
    fun `settings reject an invalid batch atomically and preserve balances`() {
        val cookie = authorizeOAuth2()
        grant(ConsoleRole.OWNER)
        mockMvc
            .perform(
                put("/console/config")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        """{"values":{"app.initial-user-gpt-usages":20,"app.share-lifetime-days":0}}"""
                    )
            )
            .andExpect(status().isBadRequest)
        assertThat(overrides.count()).isZero()
        assertThat(audits.count()).isZero()
        mockMvc
            .perform(
                put("/console/config")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content(
                        """{"values":{"app.initial-user-gpt-usages":20,"app.donation-credit-price":0.2}}"""
                    )
            )
            .andExpect(status().isOk)
        assertThat(overrides.count()).isEqualTo(2)
        assertThat(users.findById(actorId).orElseThrow().gptUsagesLeft).isEqualTo(10)
        mockMvc
            .perform(
                delete("/console/config/app.initial-user-gpt-usages").cookie(cookie).with(csrf())
            )
            .andExpect(status().isOk)
        assertThat(overrides.existsById("app.initial-user-gpt-usages")).isFalse()
        grant(ConsoleRole.ADMIN)
        mockMvc
            .perform(
                put("/console/config")
                    .cookie(cookie)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"values":{"app.initial-user-gpt-usages":1}}""")
            )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `audit search respects UTC day boundaries and validates query parameters`() {
        val cookie = authorizeOAuth2()
        grant(ConsoleRole.ADMIN)
        for (instant in
            listOf("2026-01-01T23:59:59Z", "2026-01-02T23:59:59Z", "2026-01-03T00:00:00Z")) audits
            .save(
                AuditLogEntity(
                    action = "EDIT_CONFIG",
                    targetType = "config",
                    targetId = "example",
                    actorSpotifyId = actorId,
                    occurredAt = Instant.parse(instant),
                )
            )
        mockMvc
            .perform(
                get("/console/audit-logs")
                    .cookie(cookie)
                    .param("startDate", "2026-01-02")
                    .param("endDate", "2026-01-02")
                    .param("q", "edit_config")
                    .param("field", "action")
                    .param("exact", "true")
            )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.totalElements").value(1))
        mockMvc
            .perform(
                get("/console/audit-logs")
                    .cookie(cookie)
                    .param("startDate", "2026-01-03")
                    .param("endDate", "2026-01-01")
            )
            .andExpect(status().isBadRequest)
        mockMvc
            .perform(get("/console/users").cookie(cookie).param("sort", "password"))
            .andExpect(status().isBadRequest)
    }
}
