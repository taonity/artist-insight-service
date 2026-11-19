package org.taonity.artistinsightservice.user.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.exception.UserNotFoundException
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository

@Service
class SpotifyUserService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val userArtistLinkRepository: UserArtistLinkRepository,
    private val runtimeSettings: org.taonity.artistinsightservice.settings.service.RuntimeSettings,
    private val sessionCleanup: org.taonity.artistinsightservice.security.service.SessionCleanup,
    private val properties: org.taonity.artistinsightservice.common.config.AppProperties
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun findBySpotifyIdOrThrow(spotifyId: String): SpotifyUserEntity {
        return spotifyUserRepository.findById(spotifyId)
            .orElseThrow { UserNotFoundException("SpotifyUserEntity with spotifyId $spotifyId was not found in DB") }
    }

    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity? = spotifyUserRepository.findById(spotifyId).orElse(null)

    @Transactional
    fun createOrUpdateUser(spotifyUserPrincipal: SpotifyUserPrincipal) {
        findBySpotifyId(spotifyUserPrincipal.getSpotifyId())
            ?.also { foundSpotifyUser ->
                foundSpotifyUser.updateDetails(spotifyUserPrincipal.getDisplayName())
            }
            ?: spotifyUserRepository.save(
                SpotifyUserEntity(
                    spotifyUserPrincipal.getSpotifyId(),
                    spotifyUserPrincipal.getDisplayName(),
                    runtimeSettings.effective().initialUserGptUsages
                )
            )
        if (spotifyUserPrincipal.getSpotifyId() in properties.console.ownerSpotifyIds) {
            findBySpotifyIdOrThrow(spotifyUserPrincipal.getSpotifyId()).consoleRole = org.taonity.artistinsightservice.console.entity.ConsoleRole.OWNER
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