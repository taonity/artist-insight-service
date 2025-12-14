package org.taonity.artistinsightservice.infrastructure.utils

import jakarta.validation.Validator

inline fun <reified T : Any> Validator.validateOrThrow(obj: T) {
    val violations = this.validate(obj)
    require(violations.isEmpty()) {
        violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
    }
}


fun Throwable.hasCause(clazz: Class<out Throwable>): Boolean =
    clazz.isInstance(this) || (cause?.hasCause(clazz) ?: false)