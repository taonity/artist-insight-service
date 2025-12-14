package org.taonity.artistinsightservice.user.controller

import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.dto.SpotifyUserDto
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.service.SpotifyUserService

@RestController
class UserController(
    private val spotifyUserService: SpotifyUserService
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @GetMapping("/user")
    fun user(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<SpotifyUserDto> {
        LOGGER.info { "Handling /user endpoint" }
        val spotifyUserEntity: SpotifyUserEntity = spotifyUserService.findBySpotifyIdOrThrow(principal.getSpotifyId())
        return ResponseEntity.ok(SpotifyUserDto(principal.privateUserObject, spotifyUserEntity.gptUsagesLeft))
    }

    @DeleteMapping("/user")
    fun deleteUser(@AuthenticationPrincipal principal: SpotifyUserPrincipal): ResponseEntity<Void> {
        LOGGER.info { "Handling DELETE /user endpoint" }
        spotifyUserService.deleteUserBySpotifyId(principal.getSpotifyId())
        return ResponseEntity.noContent().build()
    }
}