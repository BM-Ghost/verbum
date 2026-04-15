package com.verbum.feature.community.domain.model

data class CommunityPost(
    val id: String,
    val author: PostAuthor,
    val content: String,
    val verseReference: String? = null,
    val verseText: String? = null,
    val tags: List<String> = emptyList(),
    val amenCount: Int = 0,
    val commentCount: Int = 0,
    val createdAt: Long,
    val hasUserAmened: Boolean = false,
)

data class PostAuthor(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
)

data class BibleStudyGroup(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val coverImageUrl: String? = null,
    val currentStudyReference: String? = null,
)
