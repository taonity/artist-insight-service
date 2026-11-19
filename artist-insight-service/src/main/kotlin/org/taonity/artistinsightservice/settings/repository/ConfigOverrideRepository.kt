package org.taonity.artistinsightservice.settings.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.taonity.artistinsightservice.settings.entity.ConfigOverrideEntity

interface ConfigOverrideRepository : JpaRepository<ConfigOverrideEntity, String>
