package com.verbum.feature.profile.ui

import com.verbum.feature.profile.domain.model.ProfileStats
import com.verbum.feature.profile.domain.model.UserProfile

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(
        val profile: UserProfile,
        val stats: ProfileStats,
    ) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
