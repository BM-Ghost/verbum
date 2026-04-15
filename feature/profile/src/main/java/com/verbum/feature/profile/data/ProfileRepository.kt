package com.verbum.feature.profile.data

import com.verbum.feature.profile.domain.model.ProfileStats
import com.verbum.feature.profile.domain.model.UserProfile

interface ProfileRepository {
    suspend fun getProfile(): UserProfile
    suspend fun getStats(userId: String): ProfileStats
    suspend fun updateProfile(profile: UserProfile)
    suspend fun signOut()
}
