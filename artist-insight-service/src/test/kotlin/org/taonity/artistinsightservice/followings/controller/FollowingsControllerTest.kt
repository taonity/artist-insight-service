package org.taonity.artistinsightservice.followings.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.taonity.artistinsightservice.artist.dto.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.artist.dto.FollowingsResponse
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass

@Sql("classpath:sql/test-data.sql")
@Sql("classpath:sql/clear-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class FollowingsControllerTest: ControllerTestsBaseClass() {

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
    }

    @Test
    fun `followings raw`() {
        val session = authorizeOAuth2()

        val mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/followings").session(session))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val response: FollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        Assertions.assertThat(response.artists).hasSize(5)

        val withGenre = response.artists[0]
        Assertions.assertThat(withGenre.artistObject.id).isEqualTo("artist-with-genre")
        Assertions.assertThat(withGenre.artistObject.genres).containsExactly("metal", "rock")
        Assertions.assertThat(withGenre.genreEnriched).isFalse()

        val withoutGenre = response.artists[1]
        Assertions.assertThat(withoutGenre.artistObject.id).isEqualTo("artist-without-genre")
        Assertions.assertThat(withoutGenre.artistObject.genres).isEmpty()
        Assertions.assertThat(withoutGenre.genreEnriched).isFalse()
    }

    @Test
    fun `followings enriched`() {
        val session = authorizeOAuth2()

        val mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/followings/enriched").session(session))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val response: EnrichedFollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        Assertions.assertThat(response.artists).hasSize(5)

        val withGenre = response.artists[0]
        Assertions.assertThat(withGenre.artistObject.id).isEqualTo("artist-with-genre")
        Assertions.assertThat(withGenre.artistObject.genres).containsExactly("metal", "rock")
        Assertions.assertThat(withGenre.genreEnriched).isFalse()

        val enriched = response.artists[1]
        Assertions.assertThat(enriched.artistObject.id).isEqualTo("artist-without-genre")
        Assertions.assertThat(enriched.artistObject.genres).containsExactly(
            "Ambient",
            "Dungeon Synth",
            "Dark Ambient",
            "Electronic"
        )
        Assertions.assertThat(enriched.genreEnriched).isTrue()
    }
}