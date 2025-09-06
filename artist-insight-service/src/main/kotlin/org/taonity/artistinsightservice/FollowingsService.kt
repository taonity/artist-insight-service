package org.taonity.artistinsightservice

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.mvc.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.mvc.FollowingsResponse
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.artistinsightservice.spotify.SpotifyService

@Service
class FollowingsService(
    private val newArtistEnrichmentService: NewArtistEnrichmentService,
    private val userArtistEnrichmentService: UserArtistEnrichmentService,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyService: SpotifyService,
    private val responseAttachments: ResponseAttachments
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun fetchRawFollowings(spotifyId: String): FollowingsResponse {
        val safeFollowings = spotifyService.fetchFollowings()
        val userFollowings = userArtistEnrichmentService.enrichUserArtists(spotifyId, safeFollowings)
        return FollowingsResponse(artists = userFollowings, advisories = responseAttachments.advisoryDtos())
    }

    fun fetchGenreEnrichedFollowings(spotifyId: String): EnrichedFollowingsResponse {
        val safeFollowings: List<SafeArtistObject> = spotifyService.fetchFollowings()
        val enrichedFollowings = newArtistEnrichmentService.enrichNewArtists(spotifyId, safeFollowings)
        val userGptUsagesLeft = spotifyUserService.findBySpotifyIdOrThrow(spotifyId).gptUsagesLeft
        return EnrichedFollowingsResponse(enrichedFollowings, gptUsagesLeft = userGptUsagesLeft, advisories = responseAttachments.advisoryDtos())
    }
}
