package org.taonity.artistinsightservice.followings.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.artist.dto.EnrichedFollowingsResponse
import org.taonity.artistinsightservice.artist.dto.FollowingsResponse
import org.taonity.artistinsightservice.followings.service.FollowingsService
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal

@RestController
class FollowingsController(
    private val followingsService: FollowingsService,
) {
    @GetMapping("/followings")
    fun followings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<FollowingsResponse> {
        val followingsResponse: FollowingsResponse = followingsService.fetchRawFollowings(principal.getSpotifyId())
        return ResponseEntity.ok(followingsResponse)
    }

    @GetMapping("/followings/enriched")
    fun genreEnrichedFollowings(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<EnrichedFollowingsResponse> {
        val enrichedFollowingsResponse: EnrichedFollowingsResponse =
            followingsService.fetchGenreEnrichedFollowings(principal.getSpotifyId())
        return ResponseEntity.ok(enrichedFollowingsResponse)
    }
}