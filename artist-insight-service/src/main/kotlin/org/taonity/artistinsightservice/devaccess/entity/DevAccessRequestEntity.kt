package org.taonity.artistinsightservice.devaccess.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestStatus
import java.time.OffsetDateTime

@Entity
@Table(name = "development_access_requests")
data class DevAccessRequestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val email: String,

    val message: String? = null,

    val ipAddress: String? = null,

    val userAgent: String? = null,

    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    @Enumerated(EnumType.STRING)
    val status: DevAccessRequestStatus = DevAccessRequestStatus.PENDING
)