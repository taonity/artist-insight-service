package org.taonity.artistinsightservice.persistence.app_settings

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface AppSettingsRepository : CrudRepository<AppSettingsEntity, String>