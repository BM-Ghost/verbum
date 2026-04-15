package com.verbum.feature.community.domain

import com.verbum.feature.community.data.CommunityRepository
import com.verbum.feature.community.domain.model.CommunityPost
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityFeedUseCase @Inject constructor(
    private val repository: CommunityRepository,
) {
    operator fun invoke(): Flow<List<CommunityPost>> = repository.getCommunityFeed()
}
