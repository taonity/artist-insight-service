package org.taonity.artistinsightservice.persistence.artist

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.persistence.user.UserArtistLinkRepository

@Service
class ArtistEnrichmentService(
    private val artistGenreRepository: ArtistGenreRepository,
    private val userArtistLinkRepository: UserArtistLinkRepository
) {

    @Transactional(readOnly = true)
    fun getArtistEnrichmentInfo(artistId: String, spotifyId: String): ArtistEnrichmentInfo {
        val genres = artistGenreRepository.findGenresByArtistId(artistId)
        val isLinkedToUser = userArtistLinkRepository.existsByUserSpotifyIdAndArtistArtistId(spotifyId, artistId)
        return ArtistEnrichmentInfo(genres, isLinkedToUser)
    }
}
