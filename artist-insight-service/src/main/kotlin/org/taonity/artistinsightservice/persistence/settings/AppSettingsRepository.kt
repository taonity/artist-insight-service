package org.taonity.artistinsightservice.persistence.settings

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AppSettingsRepository : CrudRepository<AppSettingsEntity, Int> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AppSettingsEntity a where a.id = :id")
    fun findByIdForUpdate(@Param("id") id: Int): AppSettingsEntity?
}
