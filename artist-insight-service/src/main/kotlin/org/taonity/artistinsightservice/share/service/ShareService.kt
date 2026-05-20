package org.taonity.artistinsightservice.share.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.share.dto.ShareLinkResponse
import org.taonity.artistinsightservice.share.dto.ShareOwnerInfo
import org.taonity.artistinsightservice.share.dto.SharedArtist
import org.taonity.artistinsightservice.share.dto.SharedArtistsResponse
import org.taonity.artistinsightservice.share.entity.SharedLinkEntity
import org.taonity.artistinsightservice.share.exception.ShareLinkExpiredException
import org.taonity.artistinsightservice.share.exception.ShareLinkNotFoundException
import org.taonity.artistinsightservice.share.repository.SharedLinkArtistRepository
import org.taonity.artistinsightservice.share.repository.SharedLinkRepository
import org.taonity.artistinsightservice.integration.spotify.service.SpotifyService
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.user.service.SpotifyUserService
import java.security.SecureRandom
import java.time.OffsetDateTime

@Service
class ShareService(
    private val sharedLinkRepository: SharedLinkRepository,
    private val sharedLinkArtistRepository: SharedLinkArtistRepository,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyService: SpotifyService,
    private val artistRepository: ArtistRepository
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val SHARE_CODE_LENGTH = 8
        private const val SHARE_CODE_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        private const val EXPIRATION_DAYS = 30L
    }

    @Transactional
    fun createOrUpdateShareLink(spotifyId: String): ShareLinkResponse {
        val followings = spotifyService.fetchFollowings()
        val artistIds = followings.map { it.id }
        
        val existingLink = sharedLinkRepository.findByUserId(spotifyId)
        
        val sharedLink = if (existingLink != null) {
            existingLink.expiresAt = OffsetDateTime.now().plusDays(EXPIRATION_DAYS)
            sharedLinkArtistRepository.deleteAllBySharedLinkId(existingLink.id)
            existingLink.artists.clear()
            existingLink.addArtists(artistIds)
            sharedLinkRepository.save(existingLink)
        } else {
            val user = spotifyUserService.findBySpotifyIdOrThrow(spotifyId)
            val shareCode = generateUniqueShareCode()
            val newLink = SharedLinkEntity(
                user = user,
                shareCode = shareCode,
                expiresAt = OffsetDateTime.now().plusDays(EXPIRATION_DAYS)
            )
            newLink.addArtists(artistIds)
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
        sharedLinkRepository.deleteByUserSpotifyId(spotifyId)
    }

    @Transactional(readOnly = true)
    fun getShareLinkStatus(spotifyId: String): ShareLinkResponse? {
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
        val sharedLink = sharedLinkRepository.findByShareCodeWithArtists(shareCode)
            ?: throw ShareLinkNotFoundException("Share link not found: $shareCode")
        
        if (sharedLink.isExpired()) {
            throw ShareLinkExpiredException("Share link has expired: $shareCode")
        }

        val ownerInfo = fetchOwnerInfo(sharedLink.user.spotifyId)
        
        val artistIds = sharedLink.artists.map { it.artistId }
        
        if (artistIds.isEmpty()) {
            return SharedArtistsResponse(owner = ownerInfo, artists = emptyList())
        }
        
        val artists = spotifyService.fetchArtistsByIds(artistIds)

        // Fetch enriched genres from DB for artists the owner has enriched
        val enrichedGenresByArtistId = artistRepository
            .findByUserIdAndArtistIdsWithGenres(sharedLink.user.spotifyId, artistIds)
            .associate { it.artistId to it.genres.map { g -> g.genre } }

        val sharedArtists = artists.map { artist ->
            SharedArtist(
                artistObject = artist,
                enrichedGenres = enrichedGenresByArtistId[artist.id] ?: emptyList()
            )
        }

        return SharedArtistsResponse(
            owner = ownerInfo,
            artists = sharedArtists
        )
    }

    private fun fetchOwnerInfo(spotifyId: String): ShareOwnerInfo {
        val userProfile = spotifyService.fetchUserPublicProfile(spotifyId)
        val avatarUrl = userProfile.images?.firstOrNull()?.url
        return ShareOwnerInfo(
            displayName = userProfile.displayName ?: "Unknown User",
            avatarUrl = avatarUrl
        )
    }

    private fun generateUniqueShareCode(): String {
        // 62^8 ≈ 2.18e14 combinations: collision probability is negligible for our scale,
        // so we rely on the `share_code` UNIQUE constraint to surface the (astronomically
        // rare) duplicate instead of pre-checking with a SELECT on every insert.
        val random = SecureRandom()
        return (1..SHARE_CODE_LENGTH)
            .map { SHARE_CODE_CHARS[random.nextInt(SHARE_CODE_CHARS.length)] }
            .joinToString("")
    }
}
