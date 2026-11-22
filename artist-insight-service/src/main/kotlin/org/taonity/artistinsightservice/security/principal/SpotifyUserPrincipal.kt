package org.taonity.artistinsightservice.security.principal

import java.io.Serializable
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.user.OAuth2User
import org.taonity.artistinsightservice.artist.dto.SafePrivateUserObject
import org.taonity.spotify.model.ImageObject

/** Persist only application-owned values, never generated Spotify model objects. */
class SpotifyUserPrincipal
private constructor(
    private val grantedAuthorities: Collection<GrantedAuthority>,
    private val spotifyId: String,
    private val displayName: String,
    private val images: List<SessionImage>,
    val email: String?,
) : OAuth2User, Serializable {
    private data class SessionImage(val url: String, val height: Int?, val width: Int?) :
        Serializable {
        companion object {
            private const val serialVersionUID = 1L
        }
    }

    val privateUserObject: SafePrivateUserObject
        get() =
            SafePrivateUserObject(
                spotifyId,
                displayName,
                images
                    .map { image ->
                        ImageObject().apply {
                            url = image.url
                            height = image.height
                            width = image.width
                        }
                    }
                    .toMutableList(),
            )

    override fun getName(): String = spotifyId

    override fun getAuthorities(): Collection<GrantedAuthority> = grantedAuthorities

    override fun getAttributes(): Map<String, Any> =
        mapOf("id" to spotifyId, "display_name" to displayName)

    fun getSpotifyId() = spotifyId

    fun getDisplayName() = displayName

    companion object {
        private const val serialVersionUID = 1L

        fun of(user: SafePrivateUserObject, oauthUser: OAuth2User) =
            SpotifyUserPrincipal(
                oauthUser.authorities.map { SimpleGrantedAuthority(requireNotNull(it.authority)) },
                user.id,
                user.displayName,
                user.images.map { SessionImage(requireNotNull(it.url), it.height, it.width) },
                (oauthUser.attributes["email"] as? String)?.trim()?.takeIf { it.isNotEmpty() },
            )
    }
}
