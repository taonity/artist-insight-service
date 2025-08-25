package org.taonity.artistinsightservice.persistence.artist

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreEntity
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsEntity

@Entity
@Table(name = "artist")
class ArtistEntity(
    @Id
    val artistId: String,

    val artistName: String,

    // TODO: something is wrong here
    @OneToMany(mappedBy = "artist")
    val genres: List<ArtistGenreEntity> = emptyList()
)
