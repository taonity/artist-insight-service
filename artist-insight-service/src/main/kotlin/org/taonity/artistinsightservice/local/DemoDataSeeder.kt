package org.taonity.artistinsightservice.local

import java.time.Instant
import java.util.UUID
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.entity.AuditLogEntity
import org.taonity.artistinsightservice.console.entity.ConsoleRole
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestStatus
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Component
@Profile("demo-data & local & stub-spotify & !prod & !stage")
class DemoDataSeeder(
    private val users: SpotifyUserRepository,
    private val requests: DevAccessRepository,
    private val audits: AuditLogRepository,
) {
    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun seed() {
        for ((who, role) in
            listOf(
                "owner" to ConsoleRole.OWNER,
                "admin" to ConsoleRole.ADMIN,
                "user" to ConsoleRole.NONE,
            )) {
            if (!users.existsById("stub-$who"))
                users.save(SpotifyUserEntity("stub-$who", "Local $who", 10, role))
        }
        for (n in 1..3) {
            val id = UUID.nameUUIDFromBytes("artist-insight-demo-request-$n".toByteArray())
            if (!requests.existsById(id))
                requests.save(
                    DevAccessRequestEntity(
                        id = id,
                        email = "demo$n@example.com",
                        message = "Local example request $n",
                        status = DevAccessRequestStatus.entries[n - 1],
                    )
                )
        }
        val auditId = UUID.nameUUIDFromBytes("artist-insight-demo-audit".toByteArray()).toString()
        if (!audits.existsById(auditId))
            audits.save(
                AuditLogEntity(
                    id = auditId,
                    action = "DEMO",
                    targetType = "demo",
                    targetId = "local-fixture",
                    actorSpotifyId = "stub-owner",
                    occurredAt = Instant.parse("2026-01-01T12:00:00Z"),
                )
            )
    }
}
