package org.taonity.artistinsightservice.share.dto

import java.time.OffsetDateTime

data class ShareLinkResponse(
    val shareCode: String,
    val expiresAt: OffsetDateTime
)
