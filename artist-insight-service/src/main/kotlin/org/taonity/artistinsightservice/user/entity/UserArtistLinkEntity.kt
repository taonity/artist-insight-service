package org.taonity.artistinsightservice.user.entity

import jakarta.persistence.*
import org.taonity.artistinsightservice.artist.entity.ArtistEntity
import java.io.Serializable
import java.util.Objects

@Entity
@Table(name = "spotify_user_enriched_artists")
@IdClass(UserArtistLinkId::class)
class UserArtistLinkEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spotify_id", nullable = false)
    val user: SpotifyUserEntity,
    @Id
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: ArtistEntity
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserArtistLinkEntity) return false
        return user.spotifyId == other.user.spotifyId && artist.artistId == other.artist.artistId
    }

    override fun hashCode(): Int = Objects.hash(user.spotifyId, artist.artistId)
}

data class UserArtistLinkId(
    val user: String = "",
    val artist: String = ""
) : Serializable
