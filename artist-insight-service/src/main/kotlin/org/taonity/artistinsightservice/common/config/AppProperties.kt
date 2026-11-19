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
    @field:Valid val cookie: Cookie = Cookie(),
    @field:Min(0) val initialUserGptUsages: Int = 10,
    @field:Min(1) val initialGlobalGptUsages: Int = 50,
    @field:DecimalMin("0.000001") val donationCreditPrice: BigDecimal = BigDecimal("0.1"),
    @field:Min(1) @field:Max(3650) val shareLifetimeDays: Int = 30,
    @field:Valid val console: Console = Console(),
    @field:Valid val retention: Retention = Retention(),
) {
    data class Cookie(
        val secure: Boolean = true,
        @field:Pattern(regexp = "Lax|Strict|None") val sameSite: String = "Lax",
    )

    data class Console(
        val ownerSpotifyIds: List<String> = emptyList(),
        @field:Min(10) @field:Max(1000) val maxPageSize: Int = 100,
    )

    data class Retention(
        @field:Min(1) @field:Max(3650) val auditDays: Int = 90,
        val auditCron: String = "0 30 3 * * *",
    )
}
