package com.verbum.feature.community.data

import com.verbum.core.database.dao.CommunityDao
import com.verbum.core.network.api.VerbumApi
import com.verbum.feature.community.domain.model.CommunityPost
import com.verbum.feature.community.domain.model.PostAuthor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CommunityRepositoryImpl @Inject constructor(
    private val communityDao: CommunityDao,
    private val verbumApi: VerbumApi,
) : CommunityRepository {

    override fun getCommunityFeed(): Flow<List<CommunityPost>> {
        return communityDao.observeAllPosts().map { entities ->
            entities.map { entity ->
                CommunityPost(
                    id = entity.id,
                    author = PostAuthor(
                        id = entity.authorId,
                        displayName = entity.authorName,
                        avatarUrl = entity.authorAvatarUrl,
                    ),
                    content = entity.content,
                    verseReference = entity.verseReference,
                    verseText = entity.verseText,
                    tags = entity.tags.split(",").filter { it.isNotBlank() },
                    amenCount = entity.amenCount,
                    commentCount = entity.commentCount,
                    createdAt = entity.createdAt,
                )
            }
        }
    }

    override suspend fun amenPost(postId: String) {
        verbumApi.amenPost(postId)
    }

    override suspend fun createPost(content: String, verseReference: String?, tags: List<String>) {
        // TODO: Wire to API and insert into local DB
    }
}
