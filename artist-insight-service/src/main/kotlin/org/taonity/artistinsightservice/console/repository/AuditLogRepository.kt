package org.taonity.artistinsightservice.console.repository

import java.time.Instant
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.taonity.artistinsightservice.console.entity.AuditLogEntity

interface AuditLogRepository :
    JpaRepository<AuditLogEntity, String>, JpaSpecificationExecutor<AuditLogEntity> {
    fun deleteByOccurredAtBefore(cutoff: Instant): Long
}
