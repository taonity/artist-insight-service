package org.taonity.artistinsightservice.settings.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.time.Instant
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.common.config.AppProperties
import org.taonity.artistinsightservice.console.exception.ConsoleValidationException
import org.taonity.artistinsightservice.console.service.AccessGuard
import org.taonity.artistinsightservice.console.service.AuditService
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.settings.entity.ConfigOverrideEntity
import org.taonity.artistinsightservice.settings.repository.ConfigOverrideRepository

data class EffectiveSettings(
    val initialUserGptUsages: Int,
    val donationCreditPrice: BigDecimal,
    val shareLifetimeDays: Int,
    val auditDays: Int,
    val auditCron: String,
    val maxPageSize: Int,
)

data class SettingField(
    val key: String,
    val label: String,
    val type: String,
    val value: Any,
    val defaultValue: Any,
    val overridden: Boolean,
    val min: String? = null,
    val max: String? = null,
)

@Service
class RuntimeSettings(
    private val properties: AppProperties,
    private val overrides: ConfigOverrideRepository,
    private val mapper: ObjectMapper,
    private val guard: AccessGuard,
    private val audit: AuditService,
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    private val defaults
        get() =
            EffectiveSettings(
                properties.initialUserGptUsages,
                properties.donationCreditPrice,
                properties.shareLifetimeDays,
                properties.retention.auditDays,
                properties.retention.auditCron,
                properties.console.maxPageSize,
            )

    private val fields =
        listOf(
            "app.initial-user-gpt-usages",
            "app.donation-credit-price",
            "app.share-lifetime-days",
            "app.retention.audit-days",
            "app.retention.audit-cron",
            "app.console.max-page-size",
        )

    // Read committed database values instead of publishing a process-local cache before commit.
    // This also keeps multiple application instances consistent without an invalidation service.
    @Transactional(readOnly = true)
    fun effective(): EffectiveSettings =
        overrides.findAll().fold(defaults) { current, row ->
            try {
                apply(current, row.configKey, mapper.readTree(row.valueJson))
            } catch (e: Exception) {
                LOGGER.warn { "Ignoring invalid runtime setting ${row.configKey}" }
                current
            }
        }

    @Transactional(readOnly = true)
    fun schema(principal: SpotifyUserPrincipal): List<SettingField> {
        guard.requireAdmin(principal)
        val values = effective()
        val keys = overrides.findAll().map { it.configKey }.toSet()
        return fields.map { key ->
            val (label, type, min, max) =
                when (key) {
                    fields[0] -> listOf("New user GPT credits", "INT", "0", "1000000")
                    fields[1] ->
                        listOf("Donation amount per credit", "DECIMAL", "0.000001", "1000000")
                    fields[2] -> listOf("Share lifetime (days)", "INT", "1", "3650")
                    fields[3] -> listOf("Audit retention (days)", "INT", "1", "3650")
                    fields[4] -> listOf("Audit cleanup cron (UTC)", "CRON", null, null)
                    else -> listOf("Maximum admin page size", "INT", "10", "1000")
                }
            SettingField(
                key,
                label!!,
                type!!,
                read(values, key),
                read(defaults, key),
                key in keys,
                min,
                max,
            )
        }
    }

    @Transactional
    fun update(principal: SpotifyUserPrincipal, values: Map<String, JsonNode>): List<SettingField> {
        guard.requireOwner(principal)
        if (values.isEmpty()) throw ConsoleValidationException("Provide at least one setting")
        var config = effective()
        values.forEach { (key, value) -> config = apply(config, key, value) }
        values.forEach { (key, _) ->
            val before = read(effective(), key).toString()
            val value = mapper.writeValueAsString(read(config, key))
            overrides.save(
                ConfigOverrideEntity(key, value, Instant.now(), principal.getSpotifyId())
            )
            audit.record(
                principal.getSpotifyId(),
                "EDIT_CONFIG",
                "config",
                key,
                before,
                read(config, key).toString(),
            )
        }
        overrides.flush()
        return schema(principal)
    }

    @Transactional
    fun reset(principal: SpotifyUserPrincipal, key: String): List<SettingField> {
        guard.requireOwner(principal)
        if (key !in fields) throw ConsoleValidationException("Unknown setting")
        val before = read(effective(), key).toString()
        overrides.findById(key).ifPresent { overrides.delete(it) }
        overrides.flush()
        audit.record(
            principal.getSpotifyId(),
            "RESET_CONFIG",
            "config",
            key,
            before,
            read(defaults, key).toString(),
        )
        return schema(principal)
    }

    private fun read(c: EffectiveSettings, key: String): Any =
        when (key) {
            fields[0] -> c.initialUserGptUsages
            fields[1] -> c.donationCreditPrice
            fields[2] -> c.shareLifetimeDays
            fields[3] -> c.auditDays
            fields[4] -> c.auditCron
            fields[5] -> c.maxPageSize
            else -> throw ConsoleValidationException("Unknown setting")
        }

    private fun apply(c: EffectiveSettings, key: String, node: JsonNode): EffectiveSettings {
        fun int(min: Int, max: Int): Int {
            if (!node.isIntegralNumber || !node.canConvertToInt() || node.intValue() !in min..max)
                throw ConsoleValidationException("$key must be an integer between $min and $max")
            return node.intValue()
        }
        return when (key) {
            fields[0] -> c.copy(initialUserGptUsages = int(0, 1000000))
            fields[1] -> {
                if (
                    !node.isNumber ||
                        node.decimalValue() < BigDecimal("0.000001") ||
                        node.decimalValue() > BigDecimal("1000000")
                )
                    throw ConsoleValidationException("Invalid donation credit price")
                c.copy(donationCreditPrice = node.decimalValue())
            }
            fields[2] -> c.copy(shareLifetimeDays = int(1, 3650))
            fields[3] -> c.copy(auditDays = int(1, 3650))
            fields[4] -> {
                if (!node.isTextual || !CronExpression.isValidExpression(node.textValue()))
                    throw ConsoleValidationException("Invalid cron expression")
                c.copy(auditCron = node.textValue())
            }
            fields[5] -> c.copy(maxPageSize = int(10, 1000))
            else -> throw ConsoleValidationException("Unknown setting: $key")
        }
    }
}
