package org.taonity.artistinsightservice.share.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.artistinsightservice.share.dto.ShareLinkResponse
import org.taonity.artistinsightservice.share.dto.SharedArtistsResponse
import org.taonity.artistinsightservice.share.entity.SharedLinkEntity
import org.taonity.artistinsightservice.share.exception.ShareLinkExpiredException
import org.taonity.artistinsightservice.share.exception.ShareLinkNotFoundException
import org.taonity.artistinsightservice.share.repository.SharedLinkRepository
import org.taonity.artistinsightservice.integration.spotify.service.SpotifyService
import org.taonity.artistinsightservice.user.service.SpotifyUserService
import java.security.SecureRandom
import java.time.OffsetDateTime

@Service
class ShareService(
    private val sharedLinkRepository: SharedLinkRepository,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyService: SpotifyService
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val SHARE_CODE_LENGTH = 8
        private const val SHARE_CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val EXPIRATION_DAYS = 30L
    }

    @Transactional
    fun createOrUpdateShareLink(spotifyId: String): ShareLinkResponse {
        LOGGER.info { "Creating or updating share link for user: $spotifyId" }
        
        val followings = spotifyService.fetchFollowings()
        val artistIds = followings.map { it.id }
        
        val existingLink = sharedLinkRepository.findByUserId(spotifyId)
        
        val sharedLink = if (existingLink != null) {
            LOGGER.info { "Updating existing share link for user: $spotifyId" }
            existingLink.expiresAt = OffsetDateTime.now().plusDays(EXPIRATION_DAYS)
            existingLink.clearAndAddArtists(artistIds)
            sharedLinkRepository.save(existingLink)
        } else {
            LOGGER.info { "Creating new share link for user: $spotifyId" }
            val user = spotifyUserService.findBySpotifyIdOrThrow(spotifyId)
            val shareCode = generateUniqueShareCode()
            val newLink = SharedLinkEntity(
                user = user,
                shareCode = shareCode,
                expiresAt = OffsetDateTime.now().plusDays(EXPIRATION_DAYS)
            )
            newLink.clearAndAddArtists(artistIds)
            sharedLinkRepository.save(newLink)
        }
        
        LOGGER.info { "Share link created/updated with code: ${sharedLink.shareCode}, expires at: ${sharedLink.expiresAt}" }
        return ShareLinkResponse(
            shareCode = sharedLink.shareCode,
            expiresAt = sharedLink.expiresAt
        )
    }

    @Transactional
    fun deleteShareLink(spotifyId: String) {
        LOGGER.info { "Deleting share link for user: $spotifyId" }
        sharedLinkRepository.deleteByUserSpotifyId(spotifyId)
    }

    @Transactional(readOnly = true)
    fun getShareLinkStatus(spotifyId: String): ShareLinkResponse? {
        LOGGER.info { "Getting share link status for user: $spotifyId" }
        val sharedLink = sharedLinkRepository.findByUserId(spotifyId) ?: return null
        
        if (sharedLink.isExpired()) {
            return null
        }
        
        return ShareLinkResponse(
            shareCode = sharedLink.shareCode,
            expiresAt = sharedLink.expiresAt
        )
    }

    @Transactional(readOnly = true)
    fun getSharedArtists(shareCode: String): SharedArtistsResponse {
        LOGGER.info { "Fetching shared artists for share code: $shareCode" }
        
        val sharedLink = sharedLinkRepository.findByShareCodeWithArtists(shareCode)
            ?: throw ShareLinkNotFoundException("Share link not found: $shareCode")
        
        if (sharedLink.isExpired()) {
            throw ShareLinkExpiredException("Share link has expired: $shareCode")
        }
        
        val artistIds = sharedLink.artists.map { it.artistId }
        
        if (artistIds.isEmpty()) {
            return SharedArtistsResponse(artists = emptyList(), mergedGenres = emptyList())
        }
        
        val artists = spotifyService.fetchArtistsByIds(artistIds)
        val mergedGenres = mergeGenres(artists)
        
        return SharedArtistsResponse(
            artists = artists,
            mergedGenres = mergedGenres
        )
    }

    private fun mergeGenres(artists: List<SafeArtistObject>): List<String> {
        return artists
            .flatMap { it.genres }
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
    }

    private fun generateUniqueShareCode(): String {
        val random = SecureRandom()
        var code: String
        var attempts = 0
        do {
            code = (1..SHARE_CODE_LENGTH)
                .map { SHARE_CODE_CHARS[random.nextInt(SHARE_CODE_CHARS.length)] }
                .joinToString("")
            attempts++
            if (attempts > 10) {
                throw IllegalStateException("Failed to generate unique share code after $attempts attempts")
            }
        } while (sharedLinkRepository.findByShareCodeWithArtists(code) != null)
        
        return code
    }
}
