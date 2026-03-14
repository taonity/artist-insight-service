package org.taonity.artistinsightservice.devaccess.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestStatus
import java.util.UUID

@Repository
interface DevAccessRepository : JpaRepository<DevAccessRequestEntity, UUID> {

    @Transactional
    @Modifying
    @Query("UPDATE DevAccessRequestEntity e SET e.status = :status WHERE e.id = :id")
    fun updateStatus(id: UUID, status: DevAccessRequestStatus)
}
