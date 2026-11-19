package org.taonity.artistinsightservice.local

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.UUID
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.entity.AuditLogEntity
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
        DemoCatalog.people.forEach { person ->
            val existing = users.findById(person.id).orElse(null)
            if (existing == null)
                users.save(
                    SpotifyUserEntity(
                        person.id,
                        person.name,
                        person.credits,
                        person.role,
                        avatarUrl = person.avatar,
                    )
                )
            else {
                // Refresh bundled demo artwork; keep external avatars and edited account data.
                if (existing.avatarUrl?.startsWith("/demo/avatars/") == true) {
                    existing.avatarUrl = person.avatar
                }
                if (
                    existing.avatarUrl == null &&
                        existing.displayName == "Local ${person.id.removePrefix("stub-")}"
                ) {
                    existing.displayName = person.name
                    existing.avatarUrl = person.avatar
                }
            }
        }
        val anchor = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val messages =
            listOf(
                "I am building a playlist of independent artists and would like to explore the genres in my followings.",
                "Could I try Artist Insight? I follow a mix of ambient, jazz and electronic artists and would love a clearer overview.",
                "I heard about the project through a small music community. I would like to compare my listening discoveries with friends.",
                "I collect instrumental and film-score recommendations. The genre enrichment would help me organize a few hundred followings.",
                "I would like development access for my fictional demo account. My collection spans several languages and includes many artists without Spotify genres.",
                "I am curious about the share page and the CSV export. Planning a community listening night and looking for new connections between artists.",
            )
        repeat(96) { index ->
            val id = marker("request-$index")
            if (!requests.existsById(id)) {
                val person = DemoCatalog.people[(index + 8) % DemoCatalog.people.size]
                requests.save(
                    DevAccessRequestEntity(
                        id = id,
                        email = "${person.name.lowercase().replace(' ', '.')}@listeners.example",
                        message = messages[index % messages.size],
                        ipAddress = "192.0.2.${index % 200 + 1}",
                        userAgent = "DemoBrowser/1.0",
                        createdAt =
                            OffsetDateTime.ofInstant(
                                anchor.minus(index.toLong() * 7, ChronoUnit.HOURS),
                                ZoneOffset.UTC,
                            ),
                        status =
                            DevAccessRequestStatus.entries[
                                    index % DevAccessRequestStatus.entries.size],
                    )
                )
            }
        }
        val actors = DemoCatalog.people.filter { it.role.isAdmin() }
        repeat(240) { index ->
            val id = marker("audit-$index").toString()
            if (!audits.existsById(id)) {
                val action = listOf("CHANGE_ROLE", "EDIT_CONFIG", "RESET_CONFIG")[index % 3]
                val target =
                    if (action == "CHANGE_ROLE")
                        DemoCatalog.people[(index + 12) % DemoCatalog.people.size].id
                    else
                        listOf(
                            "app.share-lifetime-days",
                            "app.initial-user-gpt-usages",
                            "app.retention.audit-days",
                        )[index % 3]
                val before =
                    if (action == "CHANGE_ROLE") "NONE" else listOf("30", "10", "90")[index % 3]
                val after =
                    if (action == "CHANGE_ROLE") "ADMIN" else listOf("45", "20", "60")[index % 3]
                audits.save(
                    AuditLogEntity(
                        id = id,
                        action = action,
                        targetType = if (action == "CHANGE_ROLE") "user" else "config",
                        targetId = target,
                        actorSpotifyId = actors[index % actors.size].id,
                        occurredAt = anchor.minus(index.toLong() * 3 + 1, ChronoUnit.HOURS),
                        beforeValue = before,
                        afterValue = after,
                    )
                )
            }
        }
    }

    private fun marker(value: String): UUID =
        UUID.nameUUIDFromBytes("artist-insight-fictional-demo-v2-$value".toByteArray())
}
