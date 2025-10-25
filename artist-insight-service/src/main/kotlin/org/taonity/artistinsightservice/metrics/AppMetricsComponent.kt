package org.taonity.artistinsightservice.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.persistence.app_settings.AppSettingsRepository
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository
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