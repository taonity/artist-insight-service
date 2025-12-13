package org.taonity.artistinsightservice.openai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.openai.client.OpenAIClient
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import com.openai.models.models.Model
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.utils.hasCause
import java.io.InterruptedIOException

@Service
class OpenAIService(
    private val openAIClient: OpenAIClient
) {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    fun provideGenres(artistName: String): List<String> {
        val systemPrompt = """
            You are a music expert. When given an artist or band's name, 
            you return their genres as a JSON array.
            Return only the JSON array. No explanation.
        """.trimIndent()

        // TODO: handle prompt injection
        val userPrompt = "Provide the main genres of the artist \"$artistName\"."

        val request = chatCompletionCreateParams(systemPrompt, userPrompt)

        val response = try {
            openAIClient.chat().completions().create(request)
        } catch (e: Exception) {
            if (e.hasCause(InterruptedIOException::class.java)) {
                throw OpenAITimeoutException("OpenAI timed out", e)
            }
            throw OpenAIClientException("OpenAI completion threw an exception", e)
        }

        val content = response.choices()
            .firstOrNull()
            ?.message()
            ?.content()
            ?.orElse(null)
            ?: throw OpenAIClientException("No genre content returned from OpenAI.")

        val genres: List<String> = try {
            objectMapper.readValue(content)
        } catch (e: Exception) {
            throw OpenAIClientException("Failed to parse genres JSON: $content", e)
        }

        return genres
    }

    fun getModels() : List<Model> {
        return openAIClient.models().list().data()
    }

    private fun chatCompletionCreateParams(
        systemPrompt: String,
        userPrompt: String
    ) = ChatCompletionCreateParams.builder()
        .model(ChatModel.GPT_4)
        .messages(
            listOf(
                ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder().content(systemPrompt).build()
                ),
                ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder().content(userPrompt).build()
                )
            )
        )
        .build()
}
