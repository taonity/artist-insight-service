package org.taonity.artistinsightservice.devaccess

import jakarta.persistence.*
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