package org.taonity.artistinsightservice.config

import java.nio.file.Path
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EnvironmentProfileConfigurationTest {
    private fun runner(profile: String) =
        ApplicationContextRunner()
            .withInitializer(ConfigDataApplicationContextInitializer())
            .withSystemProperties(
                "spring.config.location=${Path.of("src/main/resources/application.yaml").toAbsolutePath().toUri()}",
                "spring.profiles.active=$profile",
            )

    @Test
    fun `profiles own their public topology without environment variables`() {
        val profiles =
            listOf(
                Triple("local", "http://127.0.0.1:3000", "LOCAL"),
                Triple("container-local", "http://127.0.0.1:3002", "LOCAL"),
                Triple("demo", "http://127.0.0.1:3008", "ARTIST-INSIGHT-DEMO"),
                Triple("stage", "https://artist-insight-stage.taonity.org", "ARTIST-INSIGHT-STAGE"),
                Triple("prod", "https://artist-insight.taonity.org", "ARTIST-INSIGHT-PROD"),
            )
        profiles.forEach { (profile, url, suffix) ->
            runner(profile).run { context ->
                assertThat(context).hasNotFailed()
                val env = context.environment
                assertThat(env.getProperty("app.default-success-url")).isEqualTo(url)
                assertThat(env.getProperty("app.login-url")).isEqualTo("$url/login")
                assertThat(env.getProperty("app.csrf-cookie-name")).isEqualTo("XSRF-TOKEN-$suffix")
                assertThat(env.getProperty("server.servlet.session.cookie.name"))
                    .isEqualTo("JSESSIONID-$suffix")
                assertThat(env.getProperty("app.cookie.secure"))
                    .isEqualTo((profile in listOf("stage", "prod")).toString())
                assertThat(env.activeProfiles.contains("demo-data")).isEqualTo(profile == "demo")
                assertThat(env.activeProfiles.contains("stub-spotify"))
                    .isEqualTo(profile in listOf("container-local", "demo"))
            }
        }
    }

    @Test
    fun `standard Spring properties can override deployment defaults`() {
        runner("prod")
            .withSystemProperties(
                "app.csrf-cookie-name=XSRF-REVIEW",
                "app.default-success-url=https://review.example.com",
            )
            .run { context ->
                assertThat(context.environment.getProperty("app.csrf-cookie-name"))
                    .isEqualTo("XSRF-REVIEW")
                assertThat(context.environment.getProperty("app.default-success-url"))
                    .isEqualTo("https://review.example.com")
            }
    }
}
