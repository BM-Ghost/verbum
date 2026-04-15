package com.verbum.feature.profile.domain

import com.verbum.feature.profile.data.ProfileRepository
import com.verbum.feature.profile.domain.model.UserProfile
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(): UserProfile = repository.getProfile()
}
