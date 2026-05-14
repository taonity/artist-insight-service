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

    @Transactional(readOnly = true)
    fun getEnrichmentInfoBatch(artistIds: List<String>, spotifyId: String): Map<String, ArtistEnrichmentInfo> {
        if (artistIds.isEmpty()) return emptyMap()
        val genresByArtist: Map<String, List<String>> = artistGenreRepository
            .findGenresByArtistIdIn(artistIds)
            .groupBy({ it.artistId }, { it.genre })
        val linkedArtistIds: Set<String> = userArtistLinkRepository
            .findAllByUserSpotifyIdAndArtistArtistIdIn(spotifyId, artistIds)
            .map { it.artist.artistId }
            .toSet()
        return artistIds.associateWith { id ->
            ArtistEnrichmentInfo(
                genres = genresByArtist[id] ?: emptyList(),
                isLinkedToUser = id in linkedArtistIds
            )
        }
    }
}
