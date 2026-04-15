package com.verbum.feature.ai.data

import com.verbum.core.network.api.VerbumApi
import com.verbum.core.network.dto.AiChatRequestDto
import com.verbum.feature.ai.domain.model.AiMessage
import com.verbum.feature.ai.domain.model.AiRole
import java.util.UUID
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val verbumApi: VerbumApi,
) : AiRepository {

    override suspend fun sendMessage(
        userMessage: String,
        verseContext: String?,
        liturgicalSeason: String?,
        conversationId: String?,
    ): AiMessage {
        val response = verbumApi.sendAiMessage(
            AiChatRequestDto(
                prompt = userMessage,
                verseContext = verseContext,
                liturgicalSeason = liturgicalSeason,
                conversationId = conversationId,
            )
        )

        return AiMessage(
            id = UUID.randomUUID().toString(),
            content = response.response,
            role = AiRole.ASSISTANT,
            relatedVerses = response.relatedVerses.orEmpty(),
            suggestedPrayer = response.suggestedPrayer,
        )
    }
}
