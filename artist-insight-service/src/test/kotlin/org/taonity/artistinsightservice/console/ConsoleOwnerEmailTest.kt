package org.taonity.artistinsightservice.console

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.DefaultOAuth2User
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.artist.dto.SafePrivateUserObject
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import org.taonity.artistinsightservice.user.service.SpotifyUserService

@Sql("classpath:sql/clear-data.sql")
@TestPropertySource(properties = ["app.console.owner-emails[0]=  MIRA.VELL@LISTENERS.EXAMPLE  "])
class ConsoleOwnerEmailTest : ControllerTestsBaseClass() {
    @Autowired lateinit var users: SpotifyUserRepository
    @Autowired lateinit var service: SpotifyUserService

    @Test
    fun `Spotify email bootstraps an existing user without resetting credits or identity`() {
        users.save(SpotifyUserEntity("stub-owner", "Previous name", 3))
        val cookie = authorizeOAuth2("stub-owner")
        mockMvc
            .perform(get("/console/access/me").cookie(cookie))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.role").value("OWNER"))
        val owner = users.findById("stub-owner").orElseThrow()
        assertThat(owner.email).isEqualTo("mira.vell@listeners.example")
        assertThat(owner.displayName).isEqualTo("Mira Vell")
        assertThat(owner.gptUsagesLeft).isEqualTo(3)
        mockMvc
            .perform(get("/user").cookie(cookie))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").doesNotExist())
    }

    @Test
    fun `unlisted email and an ID resembling a configured email cannot bootstrap owners`() {
        val cookie = authorizeOAuth2("stub-user")
        mockMvc
            .perform(get("/console/access/me").cookie(cookie))
            .andExpect(jsonPath("$.role").value("NONE"))
        service.createOrUpdateUser(
            principal("mira.vell@listeners.example", "other@listeners.example")
        )
        assertThat(users.findById("mira.vell@listeners.example").orElseThrow().consoleRole)
            .isEqualTo(ConsoleRole.NONE)
    }

    @Test
    fun `missing or blank email never bootstraps an owner`() {
        for (email in listOf(null, "", "   ")) {
            service.createOrUpdateUser(principal("stub-owner", email))
            assertThat(users.findById("stub-owner").orElseThrow().consoleRole)
                .isEqualTo(ConsoleRole.NONE)
        }
    }

    @Test
    fun `changed email updates stored profile without resetting a persisted role or balance`() {
        users.save(
            SpotifyUserEntity(
                "stub-owner",
                "Previous name",
                27,
                ConsoleRole.OWNER,
                email = "mira.vell@listeners.example",
            )
        )
        service.createOrUpdateUser(principal("stub-owner", " changed@listeners.example "))
        val user = users.findById("stub-owner").orElseThrow()
        assertThat(user.email).isEqualTo("changed@listeners.example")
        assertThat(user.consoleRole).isEqualTo(ConsoleRole.OWNER)
        assertThat(user.gptUsagesLeft).isEqualTo(27)
    }

    @Test
    fun `configured owner protection follows persisted email instead of Spotify ID`() {
        authorizeOAuth2("stub-owner")
        val actor = authorizeOAuth2("stub-admin")
        users.save(
            users.findById("stub-admin").orElseThrow().apply { consoleRole = ConsoleRole.OWNER }
        )
        mockMvc
            .perform(
                put("/console/users/stub-owner/role")
                    .cookie(actor)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"role":"NONE"}""")
            )
            .andExpect(status().isForbidden)
        users.save(
            SpotifyUserEntity(
                "mira.vell@listeners.example",
                "Unrelated user",
                4,
                ConsoleRole.ADMIN,
                email = "different@listeners.example",
            )
        )
        mockMvc
            .perform(
                put("/console/users/mira.vell@listeners.example/role")
                    .cookie(actor)
                    .with(csrf())
                    .contentType("application/json")
                    .content("""{"role":"NONE"}""")
            )
            .andExpect(status().isOk)
    }

    @Test
    fun `login requests the email permission`() {
        val response =
            mockMvc
                .perform(
                    get("http://127.0.0.1:8080/oauth2/authorization/spotify-artist-insight-service")
                )
                .andExpect(status().is3xxRedirection)
                .andReturn()
                .response
        assertThat(response.getHeader("Location")).contains("user-read-email")
        val authorizationResponse =
            HttpClient.newHttpClient()
                .send(
                    HttpRequest.newBuilder(URI.create(response.getHeader("Location")!!))
                        .GET()
                        .build(),
                    BodyHandlers.discarding(),
                )
        assertThat(authorizationResponse.statusCode()).isEqualTo(303)
    }

    private fun principal(id: String, email: String?): SpotifyUserPrincipal {
        val attributes =
            mutableMapOf<String, Any>("id" to id, "display_name" to "Fictional listener")
        email?.let { attributes["email"] = it }
        return SpotifyUserPrincipal.of(
            SafePrivateUserObject(id, "Fictional listener", mutableListOf()),
            DefaultOAuth2User(listOf(SimpleGrantedAuthority("ROLE_USER")), attributes, "id"),
        )
    }
}
