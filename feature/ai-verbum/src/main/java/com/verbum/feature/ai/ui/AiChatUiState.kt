package com.verbum.feature.ai.ui

import com.verbum.feature.ai.domain.model.AiMessage

sealed interface AiChatUiState {
    data object Idle : AiChatUiState
    data class Active(
        val messages: List<AiMessage>,
        val isLoading: Boolean = false,
        val conversationId: String? = null,
    ) : AiChatUiState
}
