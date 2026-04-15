package com.verbum.feature.ai.domain

import com.verbum.feature.ai.data.AiRepository
import com.verbum.feature.ai.domain.model.AiMessage
import com.verbum.feature.ai.domain.model.AiRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SendAiMessageUseCaseTest {

    @Test
    fun `invoke forwards arguments to repository and returns message`() = runTest {
        val expectedMessage = AiMessage(
            id = "m-1",
            content = "Peace be with you",
            role = AiRole.ASSISTANT,
        )
        val repository = RecordingAiRepository(expectedMessage)
        val useCase = SendAiMessageUseCase(repository)

        val result = useCase(
            userMessage = "Explain John 1:1",
            verseContext = "John 1:1",
            liturgicalSeason = "EASTER",
            conversationId = "c-42",
        )

        assertEquals(expectedMessage, result)
        assertEquals("Explain John 1:1", repository.userMessage)
        assertEquals("John 1:1", repository.verseContext)
        assertEquals("EASTER", repository.liturgicalSeason)
        assertEquals("c-42", repository.conversationId)
    }

    private class RecordingAiRepository(
        private val response: AiMessage,
    ) : AiRepository {
        var userMessage: String? = null
        var verseContext: String? = null
        var liturgicalSeason: String? = null
        var conversationId: String? = null

        override suspend fun sendMessage(
            userMessage: String,
            verseContext: String?,
            liturgicalSeason: String?,
            conversationId: String?,
        ): AiMessage {
            this.userMessage = userMessage
            this.verseContext = verseContext
            this.liturgicalSeason = liturgicalSeason
            this.conversationId = conversationId
            return response
        }
    }
}
