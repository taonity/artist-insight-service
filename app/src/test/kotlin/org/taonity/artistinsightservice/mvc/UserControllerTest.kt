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
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.RequestPostProcessor
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.SafePrivateUserObject
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal
import org.taonity.spotify.model.ImageObject


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:1.0-SNAPSHOT:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Sql(scripts = ["classpath:sql/test-data.sql"])
//TODO: pay attention
@DirtiesContext
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
        private val AUTHORITIES: Collection<GrantedAuthority> = emptyList()
        private const val USER_ID = "3126nx54y24ryqyza3qxcchi4wry"
        private const val USER_DISPLAY_NAME = "TestUser"
        private const val USER_GPT_USAGES_LEFT = 10
        private val USER_IMAGES = arrayListOf(
            ImageObject().apply {
                url = "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228"
                height = 300
                width = 300
            }
        )
        private val SPOTIFY_USER_PRINCIPAL = SpotifyUserPrincipal(
            authorities = emptyList(),
            attributes = emptyMap(),
            privateUserObject = SafePrivateUserObject(
                id = USER_ID,
                displayName = USER_DISPLAY_NAME,
                images = USER_IMAGES
            ),
            nameAttributeKey = USER_DISPLAY_NAME
        )
        private val authentication: RequestPostProcessor = authentication(
            OAuth2AuthenticationToken(
                SPOTIFY_USER_PRINCIPAL,
                AUTHORITIES,
                "spotify-artist-insight-service"
            )
        )
        private val EXPECTED_SPOTIFY_USER_DTO = SpotifyUserDto(
            SafePrivateUserObject(
                USER_ID,
                USER_DISPLAY_NAME,
                USER_IMAGES
            ),
            USER_GPT_USAGES_LEFT
        )
    }


    @Test
    fun `get user`() {
        val mvcJsonResult = mockMvc.perform(
            get("/user")
                .with(authentication)
        )
            .andExpect(status().isOk)
            .andReturn()
            .response
            .contentAsString

        val spotifyUserDto: SpotifyUserDto = OBJECT_MAPPER.readValue(mvcJsonResult)

        assertThat(spotifyUserDto).isEqualTo(EXPECTED_SPOTIFY_USER_DTO)
    }
}