package org.taonity.artistinsightservice.security
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
@Sql("classpath:sql/clear-data.sql")
class AlternateSpotifyLoginTest : ControllerTestsBaseClass() {
    @Test fun `stub role logins use their own authorized client for Spotify API calls`() {
        for (registration in listOf("stub-owner", "stub-admin", "stub-user")) {
            val cookie = authorizeOAuth2(registration)
            mockMvc.perform(get("/user").cookie(cookie)).andExpect(status().isOk).andExpect(jsonPath("$.privateUserObject.id").value(registration))
            mockMvc.perform(get("/followings").cookie(cookie)).andExpect(status().isOk).andExpect(jsonPath("$.artists.length()").value(5))
        }
    }
}
