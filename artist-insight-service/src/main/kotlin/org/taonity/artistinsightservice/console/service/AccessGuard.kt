package org.taonity.artistinsightservice.console.service

import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.console.exception.ConsoleForbiddenException
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Service
class AccessGuard(private val users: SpotifyUserRepository) {
    fun currentUser(principal: SpotifyUserPrincipal) =
        users.findById(principal.getSpotifyId()).orElseThrow {
            ConsoleForbiddenException("Unknown user")
        }

    fun requireAdmin(principal: SpotifyUserPrincipal) =
        currentUser(principal).also {
            if (!it.consoleRole.isAdmin())
                throw ConsoleForbiddenException("Administrator access required")
        }

    fun requireOwner(principal: SpotifyUserPrincipal) =
        requireAdmin(principal).also {
            if (it.consoleRole != ConsoleRole.OWNER)
                throw ConsoleForbiddenException("Owner access required")
        }
}
