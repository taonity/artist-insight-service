package org.taonity.artistinsightservice.mvc

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.FollowingsService
import org.taonity.artistinsightservice.ResponseAttachments
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal

@RestController
class FollowingsController(
    private val followingsService: FollowingsService,
    private val responseAttachments: ResponseAttachments
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @GetMapping("/followings")
    fun followings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<FollowingsResponse> {
        LOGGER.info { "Handling /followings endpoint" }
        val followingsResponse: FollowingsResponse = followingsService.fetchRawFollowings(principal.getSpotifyId())
        followingsResponse.advisories.addAll(responseAttachments.advisoryDtos())
        return ResponseEntity.ok(followingsResponse)
    }

    @GetMapping("/followings/enriched")
    fun genreEnrichedFollowings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<FollowingsResponse> {
        LOGGER.info { "Handling /followings/enriched endpoint" }
        val followingsResponse: FollowingsResponse =
            followingsService.fetchGenreEnrichedFollowings(principal.getSpotifyId())
        return ResponseEntity.ok(followingsResponse)
    }
}