package com.verbum.feature.profile.data

import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.dao.ReadingHistoryDao
import com.verbum.core.network.api.VerbumApi
import com.verbum.feature.profile.domain.model.ProfileStats
import com.verbum.feature.profile.domain.model.UserProfile
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val verbumApi: VerbumApi,
    private val bookmarkDao: BookmarkDao,
    private val readingHistoryDao: ReadingHistoryDao,
) : ProfileRepository {

    override suspend fun getProfile(): UserProfile {
        val dto = verbumApi.getUserProfile("me")
        return UserProfile(
            id = dto.id,
            displayName = dto.displayName,
            avatarUrl = dto.avatarUrl,
            bio = dto.bio.orEmpty(),
            followersCount = dto.followersCount,
            followingCount = dto.followingCount,
        )
    }

    override suspend fun getStats(userId: String): ProfileStats {
        val bookmarkCount = bookmarkDao.getBookmarkCount()
        val readingCount = readingHistoryDao.getTotalReadings()
        val streak = readingHistoryDao.getReadingStreak()

        return ProfileStats(
            totalReadings = readingCount,
            readingStreak = streak,
            bookmarks = bookmarkCount,
            reflectionsPosted = 0,
        )
    }

    override suspend fun updateProfile(profile: UserProfile) {
        // Network call to update profile
    }

    override suspend fun signOut() {
        // Clear tokens, local caches
    }
}
