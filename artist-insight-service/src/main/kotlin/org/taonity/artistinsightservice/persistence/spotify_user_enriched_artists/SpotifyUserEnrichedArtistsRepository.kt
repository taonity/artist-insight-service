package org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpotifyUserEnrichedArtistsRepository : CrudRepository<SpotifyUserEnrichedArtistsEntity, SpotifyUserEnrichedArtistsId> {
    fun deleteAllByUserSpotifyId(spotifyId: String)
    fun existsByUserSpotifyIdAndArtistArtistId(spotifyId: String, artistId: String): Boolean
}