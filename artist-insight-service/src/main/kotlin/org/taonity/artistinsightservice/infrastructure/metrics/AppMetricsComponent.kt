package org.taonity.artistinsightservice.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.artist.repository.ArtistGenreRepository
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.settings.repository.AppSettingsRepository
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import javax.annotation.PostConstruct

@Component
class AppMetricsComponent(
    private val meterRegistry: MeterRegistry,
    private val spotifyUserRepository: SpotifyUserRepository,
    private val artistRepository: ArtistRepository,
    private val artistGenreRepository: ArtistGenreRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    companion object {
        private const val APP_USERS_COUNT: String = "app.users.count"
        private const val APP_GLOBAL_GPT_USAGES_COUNT: String = "app.global.gpt.usages.count"
        private const val APP_ARTISTS_COUNT: String = "app.artists.count"
        private const val APP_GENRES_COUNT: String = "app.genres.count"
    }

    @PostConstruct
    fun init() {
        meterRegistry.gauge(APP_USERS_COUNT, spotifyUserRepository) { it.count().toDouble() }
        meterRegistry.gauge(APP_GLOBAL_GPT_USAGES_COUNT, appSettingsRepository) {
            it.findById(0).get().globalGptUsagesLeft.toDouble()
        }
        meterRegistry.gauge(APP_ARTISTS_COUNT, artistRepository) { it.count().toDouble() }
        meterRegistry.gauge(APP_GENRES_COUNT, artistGenreRepository) { it.count().toDouble() }
    }
}