package org.taonity.artistinsightservice.donation.kofi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:+:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Sql(scripts = ["classpath:sql/clear-data.sql", "classpath:sql/test-data.sql"])
@Sql(scripts = ["classpath:sql/clear-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class KofiCallbackControllerTest : ControllerTestsBaseClass() {

    @Autowired
    lateinit var spotifyUserRepository: SpotifyUserRepository

    companion object {
        private const val VERIFICATION_TOKEN = "e9c903f3-ec7f-456c-9c14-3f14217d33e3"
        private const val TEST_SPOTIFY_ID = "3126nx54y24ryqyza3qxcchi4wry"

        private fun buildKofiWebhookJson(
            verificationToken: String = VERIFICATION_TOKEN,
            message: String = TEST_SPOTIFY_ID,
            amount: String = "5.00"
        ): String {
            return """
                {
                    "verification_token": "$verificationToken",
                    "message_id": "test-msg-001",
                    "timestamp": "2025-01-01T00:00:00Z",
                    "type": "Donation",
                    "is_public": true,
                    "from_name": "TestDonor",
                    "message": "$message",
                    "amount": "$amount",
                    "url": "https://ko-fi.com/test",
                    "email": "donor@example.com",
                    "currency": "USD",
                    "is_subscription_payment": false,
                    "is_first_subscription_payment": false,
                    "kofi_transaction_id": "txn-001",
                    "shop_items": null,
                    "tier_name": null,
                    "shipping": null,
                    "discord_username": "",
                    "discord_userid": ""
                }
            """.trimIndent()
        }
    }

    @Test
    fun `valid donation tops up user gpt usages`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        val kofiJson = buildKofiWebhookJson(amount = "1.00")

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", kofiJson)
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        // 1.00 / 0.1 = 10 usages topped up
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore + 10)
    }

    @Test
    fun `donation with invalid verification token is ignored`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        val kofiJson = buildKofiWebhookJson(verificationToken = "invalid-token")

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", kofiJson)
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore)
    }

    @Test
    fun `donation with empty message is ignored`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        val kofiJson = buildKofiWebhookJson(message = "")

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", kofiJson)
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore)
    }

    @Test
    fun `donation with spotify id in multi-word message tops up usages`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        val kofiJson = buildKofiWebhookJson(
            message = "hello $TEST_SPOTIFY_ID thanks",
            amount = "0.50"
        )

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", kofiJson)
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        // 0.50 / 0.1 = 5 usages
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore + 5)
    }
}
