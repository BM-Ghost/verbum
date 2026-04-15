package com.verbum.feature.ai.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.feature.ai.domain.SendAiMessageUseCase
import com.verbum.feature.ai.domain.model.AiMessage
import com.verbum.feature.ai.domain.model.AiRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val sendAiMessage: SendAiMessageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AiChatUiState>(
        AiChatUiState.Active(
            messages = listOf(
                AiMessage(
                    id = "greeting",
                    content = "Peace be with you. I am Verbum AI, your spiritual companion. " +
                        "I can help you understand Scripture, suggest prayers, and offer reflections " +
                        "grounded in Catholic teaching. How may I assist you today?",
                    role = AiRole.ASSISTANT,
                )
            )
        )
    )
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        val current = _uiState.value
        if (current !is AiChatUiState.Active) return
        if (text.isBlank()) return

        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            content = text,
            role = AiRole.USER,
        )

        _uiState.update {
            current.copy(
                messages = current.messages + userMessage,
                isLoading = true,
            )
        }

        viewModelScope.launch {
            try {
                val aiResponse = sendAiMessage(
                    userMessage = text,
                    verseContext = null,
                    liturgicalSeason = null,
                    conversationId = current.conversationId,
                )

                _uiState.update { state ->
                    if (state is AiChatUiState.Active) {
                        state.copy(
                            messages = state.messages + aiResponse,
                            isLoading = false,
                            conversationId = state.conversationId ?: UUID.randomUUID().toString(),
                        )
                    } else state
                }
            } catch (e: Exception) {
                Timber.e(e, "AI request failed")
                val errorMessage = AiMessage(
                    id = UUID.randomUUID().toString(),
                    content = "I apologize, but I'm unable to respond right now. " +
                        "Please try again, and may God's peace be with you.",
                    role = AiRole.ASSISTANT,
                )
                _uiState.update { state ->
                    if (state is AiChatUiState.Active) {
                        state.copy(
                            messages = state.messages + errorMessage,
                            isLoading = false,
                        )
                    } else state
                }
            }
        }
    }
}
