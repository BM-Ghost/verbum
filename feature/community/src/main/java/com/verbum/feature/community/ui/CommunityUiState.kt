package com.verbum.feature.community.ui

import com.verbum.feature.community.domain.model.CommunityPost

sealed interface CommunityUiState {
    data object Loading : CommunityUiState
    data class Loaded(val posts: List<CommunityPost>) : CommunityUiState
    data class Error(val message: String) : CommunityUiState
}
