package org.taonity.artistinsightservice.mvc

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.FollowingsService
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal
import java.util.*

@RestController
class SpotifyFacadeController(private val followingsService: FollowingsService) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @GetMapping("/user")
    fun user(@AuthenticationPrincipal principal: SpotifyUserPrincipal): Map<String, Any> {
        LOGGER.info { "Handling /user endpoint" }
        return Collections.singletonMap("name", principal.getDisplayName())
    }

    @GetMapping("/followings/raw")
    fun followings(): ResponseEntity<FollowingsResponse> {
        LOGGER.info { "Handling /followings/raw endpoint" }
        val followingsResponse: FollowingsResponse = followingsService.fetchRawFollowings()
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