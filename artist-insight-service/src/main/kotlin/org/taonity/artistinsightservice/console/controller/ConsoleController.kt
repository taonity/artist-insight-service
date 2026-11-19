package org.taonity.artistinsightservice.console.controller

import java.time.LocalDate
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.taonity.artistinsightservice.console.dto.*
import org.taonity.artistinsightservice.console.service.ConsoleService
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.settings.service.RuntimeSettings

@RestController
@RequestMapping("/console")
class ConsoleController(
    private val service: ConsoleService,
    private val settings: RuntimeSettings,
) {
    @GetMapping("/access/me")
    fun access(@AuthenticationPrincipal p: SpotifyUserPrincipal) = service.access(p)

    @GetMapping("/users")
    fun users(
        @AuthenticationPrincipal p: SpotifyUserPrincipal,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "spotifyId") sort: String,
        @RequestParam(defaultValue = "asc") direction: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ) = service.users(p, q, sort, direction, page, size)

    @PutMapping("/users/{id}/role")
    fun role(
        @AuthenticationPrincipal p: SpotifyUserPrincipal,
        @PathVariable id: String,
        @RequestBody body: ChangeRoleBody,
    ) = service.changeRole(p, id, body.role)

    @GetMapping("/development-access-requests")
    fun requests(
        @AuthenticationPrincipal p: SpotifyUserPrincipal,
        @RequestParam(required = false) q: String?,
        @RequestParam(defaultValue = "createdAt") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ) = service.requests(p, q, sort, direction, page, size)

    @GetMapping("/audit-logs")
    fun audits(
        @AuthenticationPrincipal p: SpotifyUserPrincipal,
        @RequestParam(required = false) q: String?,
        @RequestParam(required = false) field: String?,
        @RequestParam(defaultValue = "false") exact: Boolean,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        startDate: LocalDate?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        endDate: LocalDate?,
        @RequestParam(defaultValue = "occurredAt") sort: String,
        @RequestParam(defaultValue = "desc") direction: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
    ) = service.audits(p, q, field, exact, startDate, endDate, sort, direction, page, size)

    @GetMapping("/config")
    fun config(@AuthenticationPrincipal p: SpotifyUserPrincipal) = settings.schema(p)

    @PutMapping("/config")
    fun update(
        @AuthenticationPrincipal p: SpotifyUserPrincipal,
        @RequestBody body: UpdateConfigBody,
    ) = settings.update(p, body.values)

    @DeleteMapping("/config/{key}")
    fun reset(@AuthenticationPrincipal p: SpotifyUserPrincipal, @PathVariable key: String) =
        settings.reset(p, key)
}
