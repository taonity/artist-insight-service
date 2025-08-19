package org.taonity.artistinsightservice

data class Advisory(
    val code: String,
    val title: String,
    val detail: String,
    val severity: Severity
)

enum class Severity {
    INFO,
    WARNING,
    ERROR
}
