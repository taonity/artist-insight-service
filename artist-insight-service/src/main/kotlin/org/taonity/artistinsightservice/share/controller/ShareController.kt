package org.taonity.artistinsightservice.share.controller

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.share.dto.ShareLinkResponse
import org.taonity.artistinsightservice.share.dto.SharedArtistsResponse
import org.taonity.artistinsightservice.share.service.ShareService

@RestController
@RequestMapping("/share")
class ShareController(
    private val shareService: ShareService
) {
    @PostMapping
    fun createOrUpdateShareLink(
        @AuthenticationPrincipal principal: SpotifyUserPrincipal
    ): ResponseEntity<ShareLinkResponse> {
        val response = shareService.createOrUpdateShareLink(principal.getSpotifyId())
        return ResponseEntity.ok(response)
    }

    @GetMapping
    fun getShareLinkStatus(
        @AuthenticationPrincipal principal: SpotifyUserPrincipal
    ): ResponseEntity<ShareLinkResponse> {
        val response = shareService.getShareLinkStatus(principal.getSpotifyId())
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(response)
    }

    @DeleteMapping
    fun deleteShareLink(
        @AuthenticationPrincipal principal: SpotifyUserPrincipal
    ): ResponseEntity<Void> {
        shareService.deleteShareLink(principal.getSpotifyId())
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{shareCode}")
    fun getSharedArtists(
        @PathVariable shareCode: String
    ): ResponseEntity<SharedArtistsResponse> {
        val response = shareService.getSharedArtists(shareCode)
        return ResponseEntity.ok(response)
    }
}
