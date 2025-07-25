package org.taonity.artistinsightservice.mvc.security

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService

@Service
class OAuth2UserPersistenceService(
    private val spotifyUserService: SpotifyUserService
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {
        val oAuth2User: OAuth2User = super.loadUser(userRequest)
        val spotifyUserPrincipal: SpotifyUserPrincipal = SpotifyUserPrincipal.of(oAuth2User)
        spotifyUserService.createOrUpdateUser(spotifyUserPrincipal, userRequest!!.accessToken.tokenValue)
        return spotifyUserPrincipal
    }

}