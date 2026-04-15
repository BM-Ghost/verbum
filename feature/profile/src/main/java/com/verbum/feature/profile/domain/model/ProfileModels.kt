package com.verbum.feature.profile.domain.model

data class UserProfile(
    val id: String,
    val displayName: String,
    val email: String = "",
    val avatarUrl: String? = null,
    val bio: String = "",
    val parish: String? = null,
    val favoriteVerse: String? = null,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val joinedDate: Long = 0,
    val readingStreak: Int = 0,
    val bookmarksCount: Int = 0,
)

data class ProfileStats(
    val totalReadings: Int,
    val readingStreak: Int,
    val bookmarks: Int,
    val reflectionsPosted: Int,
)
