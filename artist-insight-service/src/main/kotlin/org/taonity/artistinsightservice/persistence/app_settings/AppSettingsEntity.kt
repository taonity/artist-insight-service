package org.taonity.artistinsightservice.persistence.app_settings

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "app_settings")
data class AppSettingsEntity(
    @Id
    val id: Int = 0,
    val globalGptUsagesLeft: Int
)