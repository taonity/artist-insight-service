package org.taonity.artistinsightservice.security.service

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.stereotype.Service

@Service
class SessionCleanup(
    private val jdbc: JdbcTemplate,
    private val sessions: FindByIndexNameSessionRepository<*>,
) {
    fun deleteUserSessionsAndCredentials(spotifyId: String) {
        jdbc.update("DELETE FROM oauth2_authorized_client WHERE principal_name = ?", spotifyId)
        sessions.findByPrincipalName(spotifyId).values.forEach { sessions.deleteById(it.id) }
    }
}
