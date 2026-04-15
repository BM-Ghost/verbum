package com.verbum.feature.community.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.community.domain.AmenPostUseCase
import com.verbum.feature.community.domain.GetCommunityFeedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityFeed: GetCommunityFeedUseCase,
    private val amenPost: AmenPostUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CommunityUiState>(CommunityUiState.Loading)
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    init {
        observeFeed()
    }

    private fun observeFeed() {
        getCommunityFeed()
            .asResult()
            .onEach { result ->
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> CommunityUiState.Loading
                    is VerbumResult.Success -> CommunityUiState.Loaded(result.data)
                    is VerbumResult.Error -> CommunityUiState.Error("Failed to load community feed")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAmenPost(postId: String) {
        viewModelScope.launch { amenPost(postId) }
    }
}
