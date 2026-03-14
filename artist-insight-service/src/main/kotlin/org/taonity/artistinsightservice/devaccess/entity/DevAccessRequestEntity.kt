package org.taonity.artistinsightservice.devaccess.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(name = "development_access_requests")
class DevAccessRequestEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    val email: String,

    val message: String? = null,

    val ipAddress: String? = null,

    val userAgent: String? = null,

    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    var status: DevAccessRequestStatus = DevAccessRequestStatus.PENDING


) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DevAccessRequestEntity

        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}