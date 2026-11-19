package org.taonity.artistinsightservice.console.config

import java.time.Instant
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.Trigger
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.scheduling.support.CronTrigger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.console.repository.AuditLogRepository
import org.taonity.artistinsightservice.settings.service.RuntimeSettings

@Configuration
class AuditRetentionConfig(
    private val settings: RuntimeSettings,
    private val cleanup: AuditCleanup,
) : SchedulingConfigurer {
    override fun configureTasks(registrar: ScheduledTaskRegistrar) {
        registrar.addTriggerTask(
            { cleanup.run() },
            Trigger { ctx ->
                CronTrigger(settings.effective().auditCron, ZoneOffset.UTC).nextExecution(ctx)
            },
        )
    }
}

@Service
class AuditCleanup(private val settings: RuntimeSettings, private val audits: AuditLogRepository) {
    @Transactional
    fun run() {
        audits.deleteByOccurredAtBefore(
            Instant.now().minus(settings.effective().auditDays.toLong(), ChronoUnit.DAYS)
        )
    }
}
