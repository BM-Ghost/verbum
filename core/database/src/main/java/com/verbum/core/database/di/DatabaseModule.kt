package com.verbum.core.database.di

import android.content.Context
import androidx.room.Room
import com.verbum.core.common.constants.VerbumConstants
import com.verbum.core.database.VerbumDatabase
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.dao.CommunityDao
import com.verbum.core.database.dao.MissalDao
import com.verbum.core.database.dao.PrayerDao
import com.verbum.core.database.dao.ReadingHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVerbumDatabase(
        @ApplicationContext context: Context,
    ): VerbumDatabase {
        return Room.databaseBuilder(
            context,
            VerbumDatabase::class.java,
            VerbumConstants.APP_DATABASE_NAME,
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBibleDao(database: VerbumDatabase): BibleDao = database.bibleDao()

    @Provides
    fun provideBookmarkDao(database: VerbumDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideMissalDao(database: VerbumDatabase): MissalDao = database.missalDao()

    @Provides
    fun providePrayerDao(database: VerbumDatabase): PrayerDao = database.prayerDao()

    @Provides
    fun provideCommunityDao(database: VerbumDatabase): CommunityDao = database.communityDao()

    @Provides
    fun provideReadingHistoryDao(database: VerbumDatabase): ReadingHistoryDao =
        database.readingHistoryDao()
}
