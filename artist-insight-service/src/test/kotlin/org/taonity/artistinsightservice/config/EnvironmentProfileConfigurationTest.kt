package org.taonity.artistinsightservice.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EnvironmentProfileConfigurationTest {
    @Test
    fun `local and deployed profiles preserve explicit cookie and URL overrides`() {
        for (profile in listOf("local", "stage", "prod")) {
            ApplicationContextRunner()
                .withInitializer(ConfigDataApplicationContextInitializer())
                .withSystemProperties("spring.profiles.active=$profile", "CSRF_COOKIE_NAME=XSRF-REVIEW", "DEFAULT_SUCCESS_URL=https://review.example.com", "APP_LOGIN_URL=https://review.example.com/login")
                .run { context ->
                    assertThat(context.environment.getProperty("app.csrf-cookie-name")).isEqualTo("XSRF-REVIEW")
                    if (profile != "local") {
                        assertThat(context.environment.getProperty("app.default-success-url")).isEqualTo("https://review.example.com")
                    }
                }
        }
    }
}
