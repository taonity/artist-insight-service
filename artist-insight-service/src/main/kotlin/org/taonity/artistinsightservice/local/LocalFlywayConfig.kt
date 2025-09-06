package org.taonity.artistinsightservice.local

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile


@Configuration
@Profile("h2")
class LocalFlywayConfig {

    @Bean
    fun cleanMigrate(): FlywayMigrationStrategy {
        return FlywayMigrationStrategy { flyway ->
            flyway.clean()
            flyway.migrate()
        }
    }
}