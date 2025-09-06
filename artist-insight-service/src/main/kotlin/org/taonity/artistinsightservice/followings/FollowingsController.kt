package org.taonity.artistinsightservice.followings

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.followings.dto.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal

@RestController
class FollowingsController(
    private val followingsService: FollowingsService,
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @GetMapping("/followings")
    fun followings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<FollowingsResponse> {
        LOGGER.info { "Handling /followings endpoint" }
        val followingsResponse: FollowingsResponse = followingsService.fetchRawFollowings(principal.getSpotifyId())
        return ResponseEntity.ok(followingsResponse)
    }

    @GetMapping("/followings/enriched")
    fun genreEnrichedFollowings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<EnrichedFollowingsResponse> {
        LOGGER.info { "Handling /followings/enriched endpoint" }
        val enrichedFollowingsResponse: EnrichedFollowingsResponse =
            followingsService.fetchGenreEnrichedFollowings(principal.getSpotifyId())
        return ResponseEntity.ok(enrichedFollowingsResponse)
    }
}