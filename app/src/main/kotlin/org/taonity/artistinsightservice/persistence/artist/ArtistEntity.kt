package org.taonity.artistinsightservice.persistence.artist

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreEntity
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsEntity

@Entity
@Table(name = "artist")
data class ArtistEntity(
    @Id
    val artistId: String,

    val artistName: String,

    @OneToMany(mappedBy = "artist", cascade = [CascadeType.ALL], orphanRemoval = true)
    val genres: Set<ArtistGenreEntity> = emptySet(),
)
