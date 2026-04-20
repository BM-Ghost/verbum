package com.verbum.core.database.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.verbum.core.common.constants.VerbumConstants
import com.verbum.core.database.VerbumDatabase
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.dao.CommunityDao
import com.verbum.core.database.dao.MissalDao
import com.verbum.core.database.dao.NoteDao
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

    private val migration1To2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prayers_category ON prayers(category)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_prayers_seasonRecommendation ON prayers(seasonRecommendation)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_missal_readings_date ON missal_readings(date)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_missal_readings_date_readingType ON missal_readings(date, readingType)")
        }
    }

    private val migration2To3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `bookId` INTEGER NOT NULL,
                    `chapter` INTEGER NOT NULL,
                    `verse` INTEGER NOT NULL,
                    `content` TEXT NOT NULL,
                    `updatedAtEpochMillis` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_notes_bookId_chapter_verse ON notes(bookId, chapter, verse)")
        }
    }

    private val migration3To4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `bible_verses_new` (
                    `languageCode` TEXT NOT NULL,
                    `bookId` INTEGER NOT NULL,
                    `chapter` INTEGER NOT NULL,
                    `verse` INTEGER NOT NULL,
                    `text` TEXT NOT NULL,
                    PRIMARY KEY(`languageCode`, `bookId`, `chapter`, `verse`),
                    FOREIGN KEY(`bookId`) REFERENCES `bible_books`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO bible_verses_new(languageCode, bookId, chapter, verse, text)
                SELECT 'la', bookId, chapter, verse, text FROM bible_verses
                """.trimIndent()
            )
            db.execSQL("DROP TABLE bible_verses")
            db.execSQL("ALTER TABLE bible_verses_new RENAME TO bible_verses")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bible_verses_bookId ON bible_verses(bookId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bible_verses_languageCode ON bible_verses(languageCode)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bible_verses_languageCode_bookId ON bible_verses(languageCode, bookId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_bible_verses_languageCode_bookId_chapter ON bible_verses(languageCode, bookId, chapter)")
        }
    }

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
                .addMigrations(migration1To2, migration2To3, migration3To4)
            .build()
    }

    @Provides
    fun provideBibleDao(database: VerbumDatabase): BibleDao = database.bibleDao()

    @Provides
    fun provideBookmarkDao(database: VerbumDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideNoteDao(database: VerbumDatabase): NoteDao = database.noteDao()

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
