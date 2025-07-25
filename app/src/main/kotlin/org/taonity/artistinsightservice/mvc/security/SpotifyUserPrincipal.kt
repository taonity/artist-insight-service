package org.taonity.artistinsightservice.mvc.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User

class SpotifyUserPrincipal(
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any>,
    private val nameAttributeKey: String
) : OAuth2User {
    override fun getName(): String {
        return nameAttributeKey
    }

    override fun getAttributes(): Map<String, Any> {
        return attributes
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    fun getSpotifyId(): String {
        return attributes["id"].toString()
    }

    fun getDisplayName(): String {
        return attributes["display_name"].toString()
    }

    companion object {
        fun of(oAuth2User: OAuth2User): SpotifyUserPrincipal {
            return SpotifyUserPrincipal(
                oAuth2User.authorities,
                oAuth2User.attributes,
                oAuth2User.name
            )
        }
    }
}