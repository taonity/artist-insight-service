package org.taonity.artistinsightservice.common.config

import jakarta.validation.Valid
import jakarta.validation.constraints.*
import java.math.BigDecimal
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

@ConfigurationProperties("app")
@Validated
data class AppProperties(
    @field:NotBlank val defaultSuccessUrl: String,
    @field:NotBlank val loginUrl: String,
    @field:Pattern(regexp = "[A-Za-z0-9_-]+") val csrfCookieName: String,
    @field:Valid val cookie: Cookie,
    @field:Min(0) val initialUserGptUsages: Int,
    @field:Min(1) val initialGlobalGptUsages: Int,
    @field:DecimalMin("0.000001") val donationCreditPrice: BigDecimal,
    @field:Min(1) @field:Max(3650) val shareLifetimeDays: Int,
    @field:Valid val console: Console,
    @field:Valid val retention: Retention,
) {
    data class Cookie(
        val secure: Boolean,
        @field:Pattern(regexp = "Lax|Strict|None") val sameSite: String,
    )

    data class Console(
        val ownerSpotifyIds: List<String>,
        @field:Min(10) @field:Max(1000) val maxPageSize: Int,
    )

    data class Retention(@field:Min(1) @field:Max(3650) val auditDays: Int, val auditCron: String)
}
