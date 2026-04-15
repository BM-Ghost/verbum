package com.verbum.feature.community.data

import com.verbum.feature.community.domain.model.CommunityPost
import kotlinx.coroutines.flow.Flow

interface CommunityRepository {
    fun getCommunityFeed(): Flow<List<CommunityPost>>
    suspend fun amenPost(postId: String)
    suspend fun createPost(content: String, verseReference: String?, tags: List<String>)
}
