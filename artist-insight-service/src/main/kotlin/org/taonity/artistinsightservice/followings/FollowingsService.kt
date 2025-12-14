package org.taonity.artistinsightservice.followings

import mu.KotlinLogging
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.attachments.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.EnrichableArtists
import org.taonity.artistinsightservice.followings.dto.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.artistinsightservice.spotify.SpotifyService

@Service
class FollowingsService(
    private val newArtistEnricherFactory: NewArtistEnricherFactory,
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

        val enrichedFollowings = safeFollowings.map { rawArtist ->
            newArtistEnricherFactory.createAndEnrich(spotifyId, rawArtist)
        }

        val userGptUsagesLeft = spotifyUserService.findBySpotifyIdOrThrow(spotifyId).gptUsagesLeft
        return EnrichedFollowingsResponse(enrichedFollowings, gptUsagesLeft = userGptUsagesLeft, advisories = responseAttachments.advisoryDtos())
    }
}
