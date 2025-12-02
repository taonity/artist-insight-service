package org.taonity.artistinsightservice.persistence.genre

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsRepository

@Service
class ArtistGenreService(
    private val artistGenreRepository: ArtistGenreRepository,
    private val enrichedArtistsRepository: SpotifyUserEnrichedArtistsRepository
) {

    @Transactional(readOnly = true)
    fun getArtistEnrichmentInfo(artistId: String, spotifyId: String): ArtistEnrichmentInfo {
        val genres = artistGenreRepository.findGenresByArtistId(artistId)
        val isLinkedToUser = enrichedArtistsRepository.existsByUserSpotifyIdAndArtistArtistId(spotifyId, artistId)
        return ArtistEnrichmentInfo(genres, isLinkedToUser)
    }
}
