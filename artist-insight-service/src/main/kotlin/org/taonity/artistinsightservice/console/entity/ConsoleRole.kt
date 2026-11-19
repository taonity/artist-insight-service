package org.taonity.artistinsightservice.console.entity

enum class ConsoleRole {
    NONE,
    ADMIN,
    OWNER;

    fun isAdmin() = this == ADMIN || this == OWNER
}
