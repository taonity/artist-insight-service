package org.taonity.artistinsightservice.console.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.entity.AuditLogEntity
import org.taonity.artistinsightservice.console.repository.AuditLogRepository

@Service
class AuditService(private val repository: AuditLogRepository) {
    @Transactional
    fun record(
        actor: String,
        action: String,
        type: String,
        id: String,
        before: String? = null,
        after: String? = null,
    ) {
        repository.save(
            AuditLogEntity(
                action = action,
                targetType = type,
                targetId = id,
                actorSpotifyId = actor,
                beforeValue = before,
                afterValue = after,
            )
        )
    }
}
