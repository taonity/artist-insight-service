package org.taonity.artistinsightservice.followings.service

import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.advisory.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.followings.dto.FollowingsResponse
import org.taonity.artistinsightservice.integration.spotify.dto.SafeArtistObject
import org.taonity.artistinsightservice.artist.service.ArtistEnrichmentService
import org.taonity.artistinsightservice.user.service.SpotifyUserService
import org.taonity.artistinsightservice.integration.spotify.service.SpotifyService

@Service
class FollowingsService(
    private val newArtistEnricherFactory: NewArtistEnricherFactory,
    private val userArtistEnrichmentService: UserArtistEnrichmentService,
    private val artistEnrichmentService: ArtistEnrichmentService,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyService: SpotifyService,
    private val responseAttachments: ResponseAttachments
) {

    fun fetchRawFollowings(spotifyId: String): FollowingsResponse {
        val safeFollowings = spotifyService.fetchFollowings()
        val userFollowings = userArtistEnrichmentService.enrichUserArtists(spotifyId, safeFollowings)
        return FollowingsResponse(artists = userFollowings, advisories = responseAttachments.advisoryDtos())
    }

    fun fetchGenreEnrichedFollowings(spotifyId: String): EnrichedFollowingsResponse {
        val safeFollowings: List<SafeArtistObject> = spotifyService.fetchFollowings()

        val enrichmentInfoByArtistId = artistEnrichmentService.getEnrichmentInfoBatch(
            safeFollowings.map { it.id }, spotifyId
        )

        val enrichedFollowings = safeFollowings.map { rawArtist ->
            newArtistEnricherFactory.createAndEnrich(spotifyId, rawArtist, enrichmentInfoByArtistId[rawArtist.id])
        }

        val userGptUsagesLeft = spotifyUserService.findBySpotifyIdOrThrow(spotifyId).gptUsagesLeft
        return EnrichedFollowingsResponse(enrichedFollowings, gptUsagesLeft = userGptUsagesLeft, advisories = responseAttachments.advisoryDtos())
    }
}
