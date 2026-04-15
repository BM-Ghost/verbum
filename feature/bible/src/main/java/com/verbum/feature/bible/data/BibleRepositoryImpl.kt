package com.verbum.feature.bible.data

import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.entity.BookmarkEntity
import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.Testament
import com.verbum.feature.bible.domain.model.Verse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BibleRepositoryImpl @Inject constructor(
    private val bibleDao: BibleDao,
    private val bookmarkDao: BookmarkDao,
) : BibleRepository {

    override fun getAllBooks(): Flow<List<BibleBook>> {
        return bibleDao.getAllBooks().map { entities ->
            entities.map { entity ->
                BibleBook(
                    id = entity.id,
                    name = entity.name,
                    abbreviation = entity.abbreviation,
                    testament = if (entity.testament == "OT") Testament.OLD else Testament.NEW,
                    totalChapters = entity.totalChapters,
                )
            }
        }
    }

    override fun getVerses(bookId: Int, chapter: Int): Flow<List<Verse>> {
        val bookFlow = bibleDao.getAllBooks().map { books ->
            books.firstOrNull { it.id == bookId }?.name.orEmpty()
        }

        return combine(
            bibleDao.getVerses(bookId, chapter),
            bookFlow,
        ) { verses, bookName ->
            verses.map { entity ->
                Verse(
                    bookId = entity.bookId,
                    bookName = bookName,
                    chapter = entity.chapter,
                    verseNumber = entity.verse,
                    text = entity.text,
                )
            }
        }
    }

    override suspend fun searchVerses(query: String): List<Verse> {
        val books = bibleDao.getAllBooks().map { list ->
            list.associate { it.id to it.name }
        }
        return bibleDao.searchVerses(query).map { entity ->
            Verse(
                bookId = entity.bookId,
                bookName = "", // resolved at UI layer if needed
                chapter = entity.chapter,
                verseNumber = entity.verse,
                text = entity.text,
            )
        }
    }

    override suspend fun toggleBookmark(bookId: Int, chapter: Int, verse: Int) {
        val existing = bookmarkDao.getBookmark(bookId, chapter, verse)
        if (existing != null) {
            bookmarkDao.deleteBookmark(existing)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(bookId = bookId, chapter = chapter, verse = verse)
            )
        }
    }

    override suspend fun getChapterCount(bookId: Int): Int {
        return bibleDao.getChapterCount(bookId) ?: 1
    }
}
