package org.taonity.artistinsightservice.settings.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import org.taonity.artistinsightservice.settings.entity.AppSettingsEntity
import org.taonity.artistinsightservice.settings.repository.AppSettingsRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

class GptUsageServiceTest {

    private val spotifyUserRepository: SpotifyUserRepository = mock()
    private val appSettingsRepository: AppSettingsRepository = mock()
    private val initialGlobalGptUsages = 50
    private val service = GptUsageService(spotifyUserRepository, appSettingsRepository, initialGlobalGptUsages)

    // --- consumeUserUsage ---

    @Test
    fun `consumeUserUsage returns true and decrements when user has usages`() {
        val user = SpotifyUserEntity("user-1", "Test", "token", 5)
        `when`(spotifyUserRepository.findByIdForUpdate("user-1")).thenReturn(user)

        val result = service.consumeUserUsage("user-1")

        assertThat(result).isTrue()
        assertThat(user.gptUsagesLeft).isEqualTo(4)
    }

    @Test
    fun `consumeUserUsage returns false when user has zero usages`() {
        val user = SpotifyUserEntity("user-1", "Test", "token", 0)
        `when`(spotifyUserRepository.findByIdForUpdate("user-1")).thenReturn(user)

        val result = service.consumeUserUsage("user-1")

        assertThat(result).isFalse()
        assertThat(user.gptUsagesLeft).isEqualTo(0)
    }

    @Test
    fun `consumeUserUsage throws when user not found`() {
        `when`(spotifyUserRepository.findByIdForUpdate("missing")).thenReturn(null)

        assertThatThrownBy { service.consumeUserUsage("missing") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    // --- consumeGlobalUsage ---

    @Test
    fun `consumeGlobalUsage creates settings when none exist`() {
        `when`(appSettingsRepository.findByIdForUpdate(0)).thenReturn(null)

        val result = service.consumeGlobalUsage()

        assertThat(result).isTrue()
        val captor = ArgumentCaptor.forClass(AppSettingsEntity::class.java)
        verify(appSettingsRepository).save(captor.capture())
        assertThat(captor.value.globalGptUsagesLeft).isEqualTo(49)
    }

    @Test
    fun `consumeGlobalUsage decrements when usages available`() {
        val settings = AppSettingsEntity(globalGptUsagesLeft = 10)
        `when`(appSettingsRepository.findByIdForUpdate(0)).thenReturn(settings)

        val result = service.consumeGlobalUsage()

        assertThat(result).isTrue()
        assertThat(settings.globalGptUsagesLeft).isEqualTo(9)
    }

    @Test
    fun `consumeGlobalUsage returns false when zero usages`() {
        val settings = AppSettingsEntity(globalGptUsagesLeft = 0)
        `when`(appSettingsRepository.findByIdForUpdate(0)).thenReturn(settings)

        val result = service.consumeGlobalUsage()

        assertThat(result).isFalse()
        assertThat(settings.globalGptUsagesLeft).isEqualTo(0)
    }

    // --- topUpUserUsage ---

    @Test
    fun `topUpUserUsage adds correct amount`() {
        val user = SpotifyUserEntity("user-1", "Test", "token", 5)
        `when`(spotifyUserRepository.findByIdForUpdate("user-1")).thenReturn(user)

        service.topUpUserUsage(3.0, "user-1")

        // 3.0 / 0.1 = 30
        assertThat(user.gptUsagesLeft).isEqualTo(35)
    }

    @Test
    fun `topUpUserUsage truncates fractional usages`() {
        val user = SpotifyUserEntity("user-1", "Test", "token", 0)
        `when`(spotifyUserRepository.findByIdForUpdate("user-1")).thenReturn(user)

        service.topUpUserUsage(0.15, "user-1")

        // 0.15 / 0.1 = 1.5 → toInt() = 1
        assertThat(user.gptUsagesLeft).isEqualTo(1)
    }

    @Test
    fun `topUpUserUsage with zero amount adds nothing`() {
        val user = SpotifyUserEntity("user-1", "Test", "token", 10)
        `when`(spotifyUserRepository.findByIdForUpdate("user-1")).thenReturn(user)

        service.topUpUserUsage(0.0, "user-1")

        assertThat(user.gptUsagesLeft).isEqualTo(10)
    }

    @Test
    fun `topUpUserUsage throws when user not found`() {
        `when`(spotifyUserRepository.findByIdForUpdate("missing")).thenReturn(null)

        assertThatThrownBy { service.topUpUserUsage(1.0, "missing") }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
