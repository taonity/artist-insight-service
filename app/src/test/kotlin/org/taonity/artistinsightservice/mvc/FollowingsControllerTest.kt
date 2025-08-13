package org.taonity.artistinsightservice.mvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
    ids = [
        "org.taonity:spotify-contracts:1.0-SNAPSHOT:stubs:8100",
        "org.taonity:openai-contracts:1.0-SNAPSHOT:stubs:8101"
    ],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
//@Sql("classpath:sql/test-data.sql")
class FollowingsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
    }

    @Test
    fun `followings raw`() {
        val session = authorizeOAuth2()

        val mvcResult = mockMvc.perform(get("/followings/raw").session(session))
            .andExpect(status().isOk)
            .andReturn()

        val response: FollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        assertThat(response.artists).hasSize(2)

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

        val response: FollowingsResponse = OBJECT_MAPPER.readValue(mvcResult.response.contentAsString)

        assertThat(response.artists).hasSize(2)

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

    private fun authorizeOAuth2(): MockHttpSession {
        val session = MockHttpSession()
        val authResult = mockMvc.perform(
            get("/oauth2/authorization/spotify-artist-insight-service").session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andReturn()
        val state = getState(authResult)
        mockMvc.perform(
            get("/login/oauth2/code/spotify-artist-insight-service")
                .session(session)
                .param(
                    "code",
                    "AQDnprqgaHL-OgQl3T8ezARXH1UehFE0uYHbsgETI88sNI_2omVFSA6XOyMjG9W1xuTb-BqazM-iDfvgf4t9n8sYqRuJwsu2gDfc6Hv0Uyz6R7IvEkeGEAb9FzVV6vDOLSf5tm2j0ZKuoFeygYMVECTK3kvb0Ee5x7Kto-XTPcIyhAqVxbdN7ebxpFOtX-tYIQLM5p343HPAxr-mClIhbSrj-9_-t_7H3bQKJO-H7rR7ka5CvUU_YFAvcdwDjskxCZMBIotpiouflrK0n7NW"
                )
                .param("state", state)
        )
            .andExpect(status().is3xxRedirection)
        return session
    }

    private fun getState(authenticationMvcResult: org.springframework.test.web.servlet.MvcResult): String {
        val location = authenticationMvcResult.response.getHeader("Location")
        val rawState = UriComponentsBuilder.fromUriString(location!!)
            .build()
            .queryParams
            .getFirst("state")
        return URLDecoder.decode(rawState, StandardCharsets.UTF_8)
    }
}
