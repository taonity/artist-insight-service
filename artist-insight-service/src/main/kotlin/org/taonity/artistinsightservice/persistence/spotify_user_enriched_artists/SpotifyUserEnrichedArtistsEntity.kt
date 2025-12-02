package org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.user.SpotifyUserEntity
import java.io.Serializable
import java.util.Objects

@Entity
@Table(name = "spotify_user_enriched_artists")
@IdClass(SpotifyUserEnrichedArtistsId::class)
class SpotifyUserEnrichedArtistsEntity(
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
        if (other !is SpotifyUserEnrichedArtistsEntity) return false
        return user.spotifyId == other.user.spotifyId && artist.artistId == other.artist.artistId
    }

    override fun hashCode(): Int = Objects.hash(user.spotifyId, artist.artistId)
}

data class SpotifyUserEnrichedArtistsId(
    val user: String = "",
    val artist: String = ""
) : Serializable