package com.verbum.core.network.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VerseOfTheDayDto(
    @Json(name = "book_name") val bookName: String,
    val chapter: Int,
    val verse: Int,
    val text: String,
    val reference: String,
)

@JsonClass(generateAdapter = true)
data class MissalReadingsDto(
    val date: String,
    @Json(name = "liturgical_season") val liturgicalSeason: String,
    @Json(name = "feast_or_memorial") val feastOrMemorial: String?,
    val readings: List<ReadingDto>,
)

@JsonClass(generateAdapter = true)
data class ReadingDto(
    val id: String,
    @Json(name = "reading_type") val readingType: String,
    val title: String,
    val reference: String,
    val text: String,
)

@JsonClass(generateAdapter = true)
data class AiChatRequestDto(
    val prompt: String,
    @Json(name = "verse_context") val verseContext: String? = null,
    @Json(name = "liturgical_season") val liturgicalSeason: String? = null,
    @Json(name = "conversation_id") val conversationId: String? = null,
)

@JsonClass(generateAdapter = true)
data class AiChatResponseDto(
    val response: String,
    @Json(name = "related_verses") val relatedVerses: List<String>? = null,
    @Json(name = "suggested_prayer") val suggestedPrayer: String? = null,
    @Json(name = "conversation_id") val conversationId: String,
)

@JsonClass(generateAdapter = true)
data class CommunityPostDto(
    val id: String,
    @Json(name = "author_id") val authorId: String,
    @Json(name = "author_name") val authorName: String,
    @Json(name = "author_avatar_url") val authorAvatarUrl: String?,
    val content: String,
    @Json(name = "verse_reference") val verseReference: String?,
    @Json(name = "verse_text") val verseText: String?,
    val tags: List<String>,
    @Json(name = "amen_count") val amenCount: Int,
    @Json(name = "comment_count") val commentCount: Int,
    @Json(name = "created_at") val createdAt: Long,
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    val id: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    val bio: String?,
    @Json(name = "followers_count") val followersCount: Int,
    @Json(name = "following_count") val followingCount: Int,
    @Json(name = "is_following") val isFollowing: Boolean,
)

@JsonClass(generateAdapter = true)
data class BibleStudyGroupDto(
    val id: String,
    val name: String,
    val description: String,
    @Json(name = "member_count") val memberCount: Int,
    @Json(name = "cover_image_url") val coverImageUrl: String?,
    @Json(name = "current_study_reference") val currentStudyReference: String?,
    @Json(name = "created_by") val createdBy: String,
)
