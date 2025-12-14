package org.taonity.artistinsightservice.infrastructure.exception

enum class ClientErrorCode {
    MISSING_FIELD,
    VALIDATION_ERROR,
    TOO_MANY_REQUESTS,
    SHARE_LINK_NOT_FOUND,
    SHARE_LINK_EXPIRED
}