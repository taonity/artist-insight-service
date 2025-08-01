package org.taonity.artistinsightservice.mvc.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import org.taonity.spotify.model.PrivateUserObject

class SpotifyUserPrincipal(
    private val authorities: Collection<GrantedAuthority>,
    private val attributes: Map<String, Any>,
    val privateUserObject: PrivateUserObject,
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
        return privateUserObject.id!!
    }

    fun getDisplayName(): String {
        return privateUserObject.displayName!!
    }

    companion object {
        fun of(privateUserObject: PrivateUserObject, oAuth2User: OAuth2User): SpotifyUserPrincipal {
            return SpotifyUserPrincipal(
                oAuth2User.authorities,
                oAuth2User.attributes,
                privateUserObject,
                privateUserObject.displayName!!
            )
        }
    }
}