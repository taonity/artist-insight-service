package org.taonity.artistinsightservice.mvc.security

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.spotify.model.PrivateUserObject

@Service
class OAuth2UserPersistenceService(
    private val spotifyUserService: SpotifyUserService,
) : DefaultOAuth2UserService() {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User: OAuth2User = super.loadUser(userRequest)

        val privateUserObject: PrivateUserObject = try {
            objectMapper.convertValue<PrivateUserObject>(oAuth2User.attributes)
        } catch (e: Exception) {
            throw RuntimeException("Failed to covert PrivateUserObject attributes map: ${oAuth2User.attributes}", e)
        }

        val spotifyUserPrincipal: SpotifyUserPrincipal = SpotifyUserPrincipal.of(privateUserObject, oAuth2User)
        spotifyUserService.createOrUpdateUser(spotifyUserPrincipal, userRequest!!.accessToken.tokenValue)
        return spotifyUserPrincipal
    }

}