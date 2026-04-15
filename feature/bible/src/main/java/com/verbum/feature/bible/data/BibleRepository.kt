package com.verbum.feature.bible.data

import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface BibleRepository {
    fun getAllBooks(): Flow<List<BibleBook>>
    fun getVerses(bookId: Int, chapter: Int): Flow<List<Verse>>
    suspend fun searchVerses(query: String): List<Verse>
    suspend fun toggleBookmark(bookId: Int, chapter: Int, verse: Int)
    suspend fun getChapterCount(bookId: Int): Int
}
