package com.verbum.feature.profile.domain

import com.verbum.feature.profile.data.ProfileRepository
import com.verbum.feature.profile.domain.model.ProfileStats
import javax.inject.Inject

class GetProfileStatsUseCase @Inject constructor(
    private val repository: ProfileRepository,
) {
    suspend operator fun invoke(userId: String): ProfileStats = repository.getStats(userId)
}
