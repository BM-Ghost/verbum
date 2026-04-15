package com.verbum.feature.community.domain

import com.verbum.feature.community.data.CommunityRepository
import javax.inject.Inject

class AmenPostUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    suspend operator fun invoke(postId: String) = repository.amenPost(postId)
}
