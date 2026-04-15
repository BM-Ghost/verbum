package com.verbum.feature.ai.domain

import com.verbum.feature.ai.data.AiRepository
import com.verbum.feature.ai.domain.model.AiMessage
import javax.inject.Inject

class SendAiMessageUseCase @Inject constructor(
    private val repository: AiRepository,
) {
    suspend operator fun invoke(
        userMessage: String,
        verseContext: String?,
        liturgicalSeason: String?,
        conversationId: String?,
    ): AiMessage = repository.sendMessage(userMessage, verseContext, liturgicalSeason, conversationId)
}
