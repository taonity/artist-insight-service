package org.taonity.artistinsightservice.artist.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.artist.repository.ArtistGenreRepository
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository

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
