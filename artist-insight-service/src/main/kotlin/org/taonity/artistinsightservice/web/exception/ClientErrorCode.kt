package org.taonity.artistinsightservice.web.exception

enum class ClientErrorCode {
    MISSING_FIELD,
    VALIDATION_ERROR,
    TOO_MANY_REQUESTS,
    SHARE_LINK_NOT_FOUND,
    SHARE_LINK_EXPIRED,
    USER_NOT_FOUND
}