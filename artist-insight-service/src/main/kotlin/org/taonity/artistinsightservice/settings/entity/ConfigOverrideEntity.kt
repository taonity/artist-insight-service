package org.taonity.artistinsightservice.settings.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "config_override")
class ConfigOverrideEntity(
    @Id val configKey: String,
    var valueJson: String,
    var updatedAt: Instant,
    var updatedBy: String,
)
