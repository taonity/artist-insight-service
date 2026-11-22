package org.taonity.artistinsightservice.user.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.console.service.AccessGuard
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.security.service.SessionCleanup
import org.taonity.artistinsightservice.settings.service.RuntimeSettings
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.exception.UserNotFoundException
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository

@Service
class SpotifyUserService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val userArtistLinkRepository: UserArtistLinkRepository,
    private val runtimeSettings: RuntimeSettings,
    private val sessionCleanup: SessionCleanup,
    private val guard: AccessGuard,
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun findBySpotifyIdOrThrow(spotifyId: String): SpotifyUserEntity {
        return spotifyUserRepository.findById(spotifyId).orElseThrow {
            UserNotFoundException("SpotifyUserEntity with spotifyId $spotifyId was not found in DB")
        }
    }

    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity? =
        spotifyUserRepository.findById(spotifyId).orElse(null)

    @Transactional
    fun createOrUpdateUser(spotifyUserPrincipal: SpotifyUserPrincipal) {
        findBySpotifyId(spotifyUserPrincipal.getSpotifyId())?.also { foundSpotifyUser ->
            foundSpotifyUser.updateDetails(spotifyUserPrincipal.getDisplayName())
            foundSpotifyUser.avatarUrl =
                spotifyUserPrincipal.privateUserObject.images.firstOrNull()?.url
            foundSpotifyUser.email = spotifyUserPrincipal.email
        }
            ?: spotifyUserRepository.save(
                SpotifyUserEntity(
                    spotifyUserPrincipal.getSpotifyId(),
                    spotifyUserPrincipal.getDisplayName(),
                    runtimeSettings.effective().initialUserGptUsages,
                    avatarUrl = spotifyUserPrincipal.privateUserObject.images.firstOrNull()?.url,
                    email = spotifyUserPrincipal.email,
                )
            )
        if (guard.isConfiguredOwner(spotifyUserPrincipal.email)) {
            findBySpotifyIdOrThrow(spotifyUserPrincipal.getSpotifyId()).consoleRole =
                ConsoleRole.OWNER
        }
    }

    @Transactional
    fun deleteUserBySpotifyId(spotifyId: String) {
        if (!spotifyUserRepository.existsById(spotifyId)) {
            LOGGER.warn { "Attempted to delete non-existent user with spotifyId $spotifyId" }
            return
        }

        sessionCleanup.deleteUserSessionsAndCredentials(spotifyId)
        userArtistLinkRepository.deleteAllByUserSpotifyId(spotifyId)
        spotifyUserRepository.deleteById(spotifyId)
    }
}
