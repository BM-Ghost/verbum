package com.verbum.feature.bible.data

import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.BibleLanguage
import com.verbum.feature.bible.domain.model.Verse
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface BibleRepository {
    fun getAllBooks(): Flow<List<BibleBook>>
    fun getVerses(book: String, chapter: Int): Flow<PagingData<Verse>>
    fun getPagedVerses(bookId: Int, chapter: Int): Flow<PagingData<Verse>>
    fun getVerses(bookId: Int, chapter: Int): Flow<List<Verse>>
    suspend fun searchVerses(query: String): List<Verse>
    suspend fun searchByReference(bookQuery: String, chapter: Int, verseStart: Int?, verseEnd: Int?): List<Verse>
    suspend fun toggleBookmark(bookId: Int, chapter: Int, verse: Int)
    suspend fun getChapterCount(bookId: Int): Int
    suspend fun getAvailableLanguages(): List<BibleLanguage>
    suspend fun getSelectedLanguageCode(): String
    suspend fun setSelectedLanguageCode(languageCode: String)

    // Reading preferences
    suspend fun getReadingTheme(): String?
    suspend fun setReadingTheme(themeId: String)
    suspend fun getReadingMode(): String?
    suspend fun setReadingMode(mode: String)

    // Reading history
    suspend fun recordReadingPosition(bookId: Int, chapter: Int, verse: Int)
    fun getLastReadPosition(): Flow<ReadingPosition?>
}

data class ReadingPosition(
    val bookId: Int,
    val chapter: Int,
    val lastVerse: Int,
    val timestamp: Long,
)
