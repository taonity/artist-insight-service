package org.taonity.artistinsightservice.donation.kofi.service

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.taonity.artistinsightservice.donation.kofi.KofiPayloadBuilder
import org.taonity.artistinsightservice.donation.kofi.exception.KofiCallbackHandlingException
import org.taonity.artistinsightservice.settings.service.GptUsageService

class KofiCallbackServiceTest {

    private val gptUsageService: GptUsageService = mock()
    private val verificationToken = "valid-token"
    private val service = KofiCallbackService(gptUsageService, verificationToken)

    @Test
    fun `valid webhook tops up usages`() {
        val spotifyId = "someSpotifyId1234567890abcdef"
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(message = spotifyId, amount = "3.00"))

        verify(gptUsageService).topUpUserUsage(3.0, spotifyId)
    }

    @Test
    fun `invalid verification token does not top up`() {
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(verificationToken = "wrong-token"))

        verifyNoInteractions(gptUsageService)
    }

    @Test
    fun `empty message does not top up`() {
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(message = ""))

        verifyNoInteractions(gptUsageService)
    }

    @Test
    fun `single word message uses it as spotify id`() {
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(message = "short"))

        verify(gptUsageService).topUpUserUsage(5.0, "short")
    }

    @Test
    fun `multi-word message finds spotify id by length`() {
        val spotifyId = "abcdefghijklmnopqrstuvwxyz12"
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(message = "hi $spotifyId thanks"))

        verify(gptUsageService).topUpUserUsage(5.0, spotifyId)
    }

    @Test
    fun `multi-word message without long enough word does not top up`() {
        service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(message = "hi there friend"))

        verifyNoInteractions(gptUsageService)
    }

    @Test
    fun `empty amount throws exception`() {
        assertThatThrownBy {
            service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(amount = ""))
        }.isInstanceOf(KofiCallbackHandlingException::class.java)
    }

    @Test
    fun `non-numeric amount throws exception`() {
        assertThatThrownBy {
            service.topUpChatGptUsages(KofiPayloadBuilder.buildJson(amount = "not-a-number"))
        }.isInstanceOf(NumberFormatException::class.java)
    }

    @Test
    fun `malformed json throws exception`() {
        assertThatThrownBy {
            service.topUpChatGptUsages("not json at all")
        }.isInstanceOf(Exception::class.java)
    }
}
