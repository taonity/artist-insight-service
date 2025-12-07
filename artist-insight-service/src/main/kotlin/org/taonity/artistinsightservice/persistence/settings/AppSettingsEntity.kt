package org.taonity.artistinsightservice.persistence.settings

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "app_settings")
class AppSettingsEntity(
    @Id
    val id: Int = 0,
    var globalGptUsagesLeft: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppSettingsEntity) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
