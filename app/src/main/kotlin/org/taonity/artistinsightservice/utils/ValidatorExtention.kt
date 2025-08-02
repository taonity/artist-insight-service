package org.taonity.artistinsightservice.utils

import jakarta.validation.Validator

inline fun <reified T : Any> Validator.validateOrThrow(obj: T) {
    val violations = this.validate(obj)
    require(violations.isEmpty()) {
        violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
    }
}