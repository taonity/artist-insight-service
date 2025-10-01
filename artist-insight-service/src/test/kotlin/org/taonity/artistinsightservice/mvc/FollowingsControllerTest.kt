package org.taonity.artistinsightservice.mvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.followings.FollowingsResponse
import org.taonity.artistinsightservice.followings.dto.EnrichedFollowingsResponse

@AutoConfigureStubRunner(
    ids = [
        "org.taonity:spotify-contracts:1.0-SNAPSHOT:stubs:8100",
        "org.taonity:openai-contracts:1.0-SNAPSHOT:stubs:8101"
    ],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Sql("classpath:sql/test-data.sql")
@Sql("classpath:sql/clear-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FollowingsControllerTest: ControllerTestsBaseClass() {

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
    }

    @Test
    fun `followings raw`() {
        val session = authorizeOAuth2()

        val mvcResult = mockMvc.perform(get("/followings").session(session))
            .andExpect(status().isOk)
            .andReturn()

        val response: FollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        assertThat(response.artists).hasSize(5)

        val withGenre = response.artists[0]
        assertThat(withGenre.artistObject.id).isEqualTo("artist-with-genre")
        assertThat(withGenre.artistObject.genres).containsExactly("metal", "rock")
        assertThat(withGenre.genreEnriched).isFalse()

        val withoutGenre = response.artists[1]
        assertThat(withoutGenre.artistObject.id).isEqualTo("artist-without-genre")
        assertThat(withoutGenre.artistObject.genres).isEmpty()
        assertThat(withoutGenre.genreEnriched).isFalse()
    }

    @Test
    fun `followings enriched`() {
        val session = authorizeOAuth2()

        val mvcResult = mockMvc.perform(get("/followings/enriched").session(session))
            .andExpect(status().isOk)
            .andReturn()

        val response: EnrichedFollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        assertThat(response.artists).hasSize(5)

        val withGenre = response.artists[0]
        assertThat(withGenre.artistObject.id).isEqualTo("artist-with-genre")
        assertThat(withGenre.artistObject.genres).containsExactly("metal", "rock")
        assertThat(withGenre.genreEnriched).isFalse()

        val enriched = response.artists[1]
        assertThat(enriched.artistObject.id).isEqualTo("artist-without-genre")
        assertThat(enriched.artistObject.genres).containsExactly(
            "Ambient",
            "Dungeon Synth",
            "Dark Ambient",
            "Electronic"
        )
        assertThat(enriched.genreEnriched).isTrue()
    }
}
