package org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.user.SpotifyUserEntity
import java.io.Serializable

@Entity
@Table(name = "spotify_user_enriched_artists")
@IdClass(SpotifyUserEnrichedArtistsId::class)
data class SpotifyUserEnrichedArtistsEntity(
    @Id
    @ManyToOne
    @JoinColumn(name = "spotify_id", nullable = false)
    val user: SpotifyUserEntity,
    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: ArtistEntity
)

data class SpotifyUserEnrichedArtistsId(
    val user: String = "",
    val artist: String = ""
) : Serializable