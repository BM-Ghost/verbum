package com.verbum.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.dao.CommunityDao
import com.verbum.core.database.dao.MissalDao
import com.verbum.core.database.dao.NoteDao
import com.verbum.core.database.dao.PrayerDao
import com.verbum.core.database.dao.ReadingHistoryDao
import com.verbum.core.database.entity.BibleBookEntity
import com.verbum.core.database.entity.BibleVerseEntity
import com.verbum.core.database.entity.BookmarkEntity
import com.verbum.core.database.entity.CommunityPostEntity
import com.verbum.core.database.entity.MissalReadingEntity
import com.verbum.core.database.entity.NoteEntity
import com.verbum.core.database.entity.PrayerEntity
import com.verbum.core.database.entity.ReadingHistoryEntity

@Database(
    entities = [
        BibleBookEntity::class,
        BibleVerseEntity::class,
        BookmarkEntity::class,
        NoteEntity::class,
        MissalReadingEntity::class,
        PrayerEntity::class,
        CommunityPostEntity::class,
        ReadingHistoryEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class VerbumDatabase : RoomDatabase() {
    abstract fun bibleDao(): BibleDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun missalDao(): MissalDao
    abstract fun prayerDao(): PrayerDao
    abstract fun communityDao(): CommunityDao
    abstract fun readingHistoryDao(): ReadingHistoryDao
}
