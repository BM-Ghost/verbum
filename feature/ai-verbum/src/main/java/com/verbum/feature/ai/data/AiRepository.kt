package com.verbum.feature.ai.data

import com.verbum.feature.ai.domain.model.AiMessage

interface AiRepository {
    suspend fun sendMessage(
        userMessage: String,
        verseContext: String?,
        liturgicalSeason: String?,
        conversationId: String?,
    ): AiMessage
}
