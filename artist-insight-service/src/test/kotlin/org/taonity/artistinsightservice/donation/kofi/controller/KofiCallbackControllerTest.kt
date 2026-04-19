package org.taonity.artistinsightservice.donation.kofi.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.donation.kofi.KofiPayloadBuilder
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Sql(scripts = ["classpath:sql/clear-data.sql", "classpath:sql/test-data.sql"])
@Sql(scripts = ["classpath:sql/clear-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class KofiCallbackControllerTest : ControllerTestsBaseClass() {

    @Autowired
    lateinit var spotifyUserRepository: SpotifyUserRepository

    companion object {
        private const val VERIFICATION_TOKEN = "e9c903f3-ec7f-456c-9c14-3f14217d33e3"
        private const val TEST_SPOTIFY_ID = "3126nx54y24ryqyza3qxcchi4wry"
    }

    @Test
    fun `valid donation tops up user gpt usages`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", KofiPayloadBuilder.buildJson(
                    verificationToken = VERIFICATION_TOKEN,
                    message = TEST_SPOTIFY_ID,
                    amount = "1.00"
                ))
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

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", KofiPayloadBuilder.buildJson(
                    verificationToken = "invalid-token",
                    message = TEST_SPOTIFY_ID
                ))
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore)
    }

    @Test
    fun `donation with empty message is ignored`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", KofiPayloadBuilder.buildJson(
                    verificationToken = VERIFICATION_TOKEN,
                    message = ""
                ))
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore)
    }

    @Test
    fun `donation with spotify id in multi-word message tops up usages`() {
        val userBefore = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        val usagesBefore = userBefore.gptUsagesLeft

        mockMvc.perform(
            post("/callback/kofi")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("data", KofiPayloadBuilder.buildJson(
                    verificationToken = VERIFICATION_TOKEN,
                    message = "hello $TEST_SPOTIFY_ID thanks",
                    amount = "0.50"
                ))
        )
            .andExpect(status().isOk)

        val userAfter = spotifyUserRepository.findById(TEST_SPOTIFY_ID).get()
        // 0.50 / 0.1 = 5 usages
        assertThat(userAfter.gptUsagesLeft).isEqualTo(usagesBefore + 5)
    }
}
