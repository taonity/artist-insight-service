package org.taonity.artistinsightservice.advisory

import java.lang.String.format

enum class Advisory (
    private val title: String,
    private val detailTemplate: String,
    private val severity: Severity,
    private val args: MutableList<String> = mutableListOf()
) {
    TOO_MANY_FOLLOWINGS(
        "Too many followings",
        "The service handles only up to 1000 followings.",
        Severity.WARNING
    ),
    GPT_ENRICHMENT_AVAILABLE(
        "GPT enrichment available",
        "You can enrich %s followings with genres using GPT completion for free!",
        Severity.INFO
    ),
    OPENAI_PROBLEM(
        "Problems with OpenAI",
        "Sorry! We have some problems with OpenAI, some artists can't be enriched now. The fix is on the way.",
        Severity.ERROR
    ),
    OPENAI_TIMEOUT(
        "OpenAI timed out",
        "Sorry! OpenAI timed out, we can't enrich your artist now. Please, try again later.",
        Severity.ERROR
    ),
    SPOTIFY_PROBLEM(
        "Problems with Spotify",
        "Sorry! We have some problems with Spotify, artists cannot be retrieved. The fix is on the way.",
        Severity.ERROR
    ),
    SPOTIFY_TIMEOUT(
        "Spotify timed out",
        "Sorry! Spotify timed out, we can't retrieve your artist now. Please, try again later.",
        Severity.ERROR
    ),
    USER_GPT_USAGES_DEPLETED(
        "User GPT usages depleted",
        "Your GPT usage account depleted, consider donation to top up your GPT usage account.",
        Severity.INFO
    ),
    GLOBAL_GPT_USAGES_DEPLETED(
        "Global GPT usages depleted",
        "Sorry! Global GPT usages for service depleted, you can't enrich artists now. The fix is on the way.",
        Severity.WARNING
    ),;

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
