package org.taonity.artistinsightservice.console.service

import jakarta.persistence.criteria.Predicate
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Locale
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.dto.*
import org.taonity.artistinsightservice.console.entity.*
import org.taonity.artistinsightservice.console.exception.*
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.settings.service.RuntimeSettings
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Service
@Transactional(readOnly = true)
class ConsoleService(
    private val users: SpotifyUserRepository,
    private val requests: DevAccessRepository,
    private val audits: AuditLogRepository,
    private val guard: AccessGuard,
    private val audit: AuditService,
    private val settings: RuntimeSettings,
) {
    fun access(p: SpotifyUserPrincipal) =
        guard.currentUser(p).let {
            AccessInfo(
                it.spotifyId,
                it.consoleRole,
                it.consoleRole.isAdmin(),
                it.consoleRole == ConsoleRole.OWNER,
            )
        }

    fun users(
        p: SpotifyUserPrincipal,
        q: String?,
        sort: String,
        direction: String,
        page: Int,
        size: Int,
    ): PageResponse<UserSummary> {
        guard.requireAdmin(p)
        return PageResponse.of(
            users
                .findAll(
                    search(q, listOf("spotifyId", "displayName")),
                    paging(
                        sort,
                        direction,
                        page,
                        size,
                        setOf("spotifyId", "displayName", "gptUsagesLeft", "consoleRole"),
                        "spotifyId",
                    ),
                )
                .map(UserSummary::from)
        )
    }

    fun requests(
        p: SpotifyUserPrincipal,
        q: String?,
        sort: String,
        direction: String,
        page: Int,
        size: Int,
    ): PageResponse<DevAccessRequestEntity> {
        guard.requireAdmin(p)
        return PageResponse.of(
            requests.findAll(
                search(q, listOf("email", "message")),
                paging(
                    sort,
                    direction,
                    page,
                    size,
                    setOf("email", "createdAt", "status"),
                    "createdAt",
                ),
            )
        )
    }

    fun audits(
        p: SpotifyUserPrincipal,
        q: String?,
        field: String?,
        exact: Boolean,
        start: LocalDate?,
        end: LocalDate?,
        sort: String,
        direction: String,
        page: Int,
        size: Int,
    ): PageResponse<AuditLogEntity> {
        guard.requireAdmin(p)
        if (
            (start != null && start.year !in 1..9999) ||
                (end != null && end.year !in 1..9999) ||
                (start != null && end != null && start > end)
        )
            throw ConsoleValidationException("Invalid date period")
        val fields = listOf("action", "targetType", "targetId", "actorSpotifyId")
        if (!field.isNullOrBlank() && field != "all" && field !in fields)
            throw ConsoleValidationException("Invalid search field")
        val spec =
            search<AuditLogEntity>(q, if (field in fields) listOf(field!!) else fields, exact)
                .and(
                    Specification { root, _, cb ->
                        val conditions = mutableListOf<Predicate>()
                        start?.let {
                            conditions +=
                                cb.greaterThanOrEqualTo(
                                    root.get("occurredAt"),
                                    it.atStartOfDay(ZoneOffset.UTC).toInstant(),
                                )
                        }
                        end?.let {
                            conditions +=
                                cb.lessThan(
                                    root.get("occurredAt"),
                                    it.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant(),
                                )
                        }
                        cb.and(*conditions.toTypedArray())
                    }
                )
        return PageResponse.of(
            audits.findAll(
                spec,
                paging(sort, direction, page, size, fields.toSet() + "occurredAt", "occurredAt"),
            )
        )
    }

    @Transactional
    fun changeRole(p: SpotifyUserPrincipal, targetId: String, role: ConsoleRole): UserSummary {
        val actor = guard.requireAdmin(p)
        if (actor.spotifyId == targetId)
            throw ConsoleForbiddenException("You cannot change your own role")
        val target =
            users.findByIdForUpdate(targetId) ?: throw ConsoleNotFoundException("User not found")
        if (actor.consoleRole != ConsoleRole.OWNER)
            throw ConsoleForbiddenException("Only owners can manage administrator roles")
        if (guard.isConfiguredOwner(target.email))
            throw ConsoleForbiddenException(
                "Configured owners must be managed through deployment configuration"
            )
        val before = target.consoleRole
        target.consoleRole = role
        audit.record(actor.spotifyId, "CHANGE_ROLE", "user", targetId, before.name, role.name)
        return UserSummary.from(target)
    }

    private fun paging(
        sort: String,
        direction: String,
        page: Int,
        size: Int,
        allowed: Set<String>,
        fallback: String,
    ): PageRequest {
        if (
            page < 0 ||
                size < 1 ||
                sort !in allowed ||
                direction.lowercase() !in setOf("asc", "desc")
        )
            throw ConsoleValidationException("Invalid pagination or sorting")
        return PageRequest.of(
            page,
            size.coerceAtMost(settings.effective().maxPageSize),
            Sort.by(Sort.Direction.fromString(direction), sort)
                .and(Sort.by(if (fallback == "spotifyId") "spotifyId" else "id")),
        )
    }

    private fun <T : Any> search(
        q: String?,
        fields: List<String>,
        exact: Boolean = false,
    ): Specification<T> {
        if ((q?.length ?: 0) > 512) throw ConsoleValidationException("Search is too long")
        return Specification { root, _, cb ->
            if (q.isNullOrBlank()) cb.conjunction()
            else {
                val term = q.trim().lowercase(Locale.ROOT)
                val escaped = term.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
                cb.or(
                    *fields
                        .map {
                            if (exact) cb.equal(cb.lower(root.get(it)), term)
                            else cb.like(cb.lower(root.get(it)), "%$escaped%", '\\')
                        }
                        .toTypedArray()
                )
            }
        }
    }
}
