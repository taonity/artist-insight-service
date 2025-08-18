package org.taonity.artistinsightservice

import java.lang.String.format

enum class Advisory (
    private val title: String,
    private val detailTemplate: String,
    private val severity: Severity,
    private val args: MutableList<String> = mutableListOf()
) {
    TOO_MANY_FOLLOWERS(
        "Too many followers",
        "The service handles only up to 1000 followers",
        Severity.WARNING
    ),
    GPT_ENRICHMENT_AVAILABLE(
        "GPT enrichment available",
        "You can enrich %s followings with genres using GPT completion for free!",
        Severity.INFO
    );

    fun withDetailArgs(vararg args: String): Advisory {
        this.args.addAll(args.toList())
        return this
    }

    fun toDto(): AdvisoryDto {
        return AdvisoryDto(
            code = this.name,
            title = this.title,
            detail = getDetail(),
            severity = this.severity
        )
    }

    private fun getDetail(): String {
        return format(detailTemplate, *args.toTypedArray())
    }
}

enum class Severity {
    INFO,
    WARNING,
    ERROR
}
