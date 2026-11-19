package org.taonity.artistinsightservice.console.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_log")
class AuditLogEntity(
    @Id val id: String = UUID.randomUUID().toString(),
    val action: String,
    val targetType: String,
    val targetId: String,
    val actorSpotifyId: String,
    val occurredAt: Instant = Instant.now(),
    val beforeValue: String? = null,
    val afterValue: String? = null,
)
