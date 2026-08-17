package com.verbum.feature.bible.data

import android.util.LruCache
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.verbum.core.common.constants.VerbumConstants
import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.core.common.preferences.BootstrapPreferences
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.dao.BookmarkDao
import com.verbum.core.database.dao.ReadingHistoryDao
import com.verbum.core.database.entity.BookmarkEntity
import com.verbum.core.database.entity.ReadingHistoryEntity
import com.verbum.feature.bible.data.seed.BibleAssetSeeder
import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.BibleLanguage
import com.verbum.feature.bible.domain.model.BibleCrossReference
import com.verbum.feature.bible.domain.model.Testament
import com.verbum.feature.bible.domain.model.Verse
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class BibleRepositoryImpl @Inject constructor(
    private val bibleDao: BibleDao,
    private val bookmarkDao: BookmarkDao,
    private val readingHistoryDao: ReadingHistoryDao,
    private val bootstrapPreferences: BootstrapPreferences,
    private val bibleAssetSeeder: BibleAssetSeeder,
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) : BibleRepository {

    private val chapterCache = LruCache<String, List<Verse>>(24)

    override suspend fun refreshDrcFromOnline(): Result<Int> = withContext(ioDispatcher) {
        bibleAssetSeeder.refreshDrcFromOnline().also { result ->
            if (result.isSuccess) chapterCache.evictAll()
        }
    }

    override fun getAllBooks(): Flow<List<BibleBook>> {
        return flow {
            emitAll(
                bibleDao.getAllBooks().map { entities ->
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
            )
        }
    }

    override fun getPagedVerses(bookId: Int, chapter: Int): Flow<PagingData<Verse>> {
        return flow {
            val language = resolveSelectedLanguageCode()
            val bookName = bibleDao.getBookById(bookId)?.name.orEmpty()
            emitAll(
                Pager(
                    config = PagingConfig(
                        pageSize = VerbumConstants.PAGE_SIZE,
                        initialLoadSize = VerbumConstants.PAGE_SIZE * 2,
                        prefetchDistance = 8,
                        enablePlaceholders = false,
                    ),
                    pagingSourceFactory = { bibleDao.getVersesPagingSource(bookId, chapter, language) },
                ).flow.map { pagingData ->
                    pagingData.map { entity ->
                        Verse(
                            bookId = entity.bookId,
                            bookName = bookName,
                            chapter = entity.chapter,
                            verseNumber = entity.verse,
                            text = entity.text,
                        )
                    }
                }
            )
        }
    }

    override fun getVerses(book: String, chapter: Int): Flow<PagingData<Verse>> {
        return flow {
            val bookId = bibleDao.getBookIdByAbbreviation(book.uppercase())
                ?: bibleDao.getBookIdByName(book)
                ?: throw IllegalArgumentException("Unknown book: $book")
            emitAll(getPagedVerses(bookId, chapter))
        }
    }

    override fun getVerses(bookId: Int, chapter: Int): Flow<List<Verse>> {
        return flow {
            val language = resolveSelectedLanguageCode()
            val key = "$language:$bookId:$chapter"
            chapterCache.get(key)?.let { emit(it) }

            val bookFlow = bibleDao.getAllBooks().map { books ->
                books.firstOrNull { it.id == bookId }?.name.orEmpty()
            }

            val selectedFlow = bibleDao.getVerses(bookId, chapter, language)
            val englishFlow = if (language != ENGLISH_LANGUAGE_CODE) {
                bibleDao.getVerses(bookId, chapter, ENGLISH_LANGUAGE_CODE)
            } else {
                selectedFlow
            }
            val latinFlow = if (language != LATIN_LANGUAGE_CODE) {
                bibleDao.getVerses(bookId, chapter, LATIN_LANGUAGE_CODE)
            } else {
                selectedFlow
            }

            emitAll(
                combine(
                    selectedFlow,
                    englishFlow,
                    latinFlow,
                    bookFlow,
                ) { selectedVerses, englishVerses, latinVerses, bookName ->
                    val resolvedVerses = when {
                        selectedVerses.isNotEmpty() -> selectedVerses
                        englishVerses.isNotEmpty() -> englishVerses
                        latinVerses.isNotEmpty() -> latinVerses
                        else -> selectedVerses
                    }

                    resolvedVerses.map { entity ->
                        Verse(
                            bookId = entity.bookId,
                            bookName = bookName,
                            chapter = entity.chapter,
                            verseNumber = entity.verse,
                            text = entity.text,
                        )
                    }.also { mapped ->
                        chapterCache.put(key, mapped)
                    }
                }
            )
        }
    }

    override suspend fun searchVerses(query: String): List<Verse> = withContext(ioDispatcher) {
        val language = resolveSelectedLanguageCode()
        val books = bibleDao.getAllBooks().first().associate { it.id to it.name }

        // Split query into individual words for multi-word matching
        val words = query.trim().split(Regex("\\s+")).filter { it.length >= 2 }

        if (words.size <= 1) {
            // Single word: standard LIKE search
            return@withContext bibleDao.searchVerses(query.trim(), language).map { entity ->
                Verse(
                    bookId = entity.bookId,
                    bookName = books[entity.bookId].orEmpty(),
                    chapter = entity.chapter,
                    verseNumber = entity.verse,
                    text = entity.text,
                )
            }
        }

        // Multi-word: search for the first word, then filter results that contain all words
        val primaryResults = bibleDao.searchVerses(words.first(), language, limit = 200)
        return@withContext primaryResults
            .filter { entity ->
                val lowerText = entity.text.lowercase()
                words.all { word -> lowerText.contains(word.lowercase()) }
            }
            .take(50)
            .map { entity ->
                Verse(
                    bookId = entity.bookId,
                    bookName = books[entity.bookId].orEmpty(),
                    chapter = entity.chapter,
                    verseNumber = entity.verse,
                    text = entity.text,
                )
            }
    }

    override suspend fun searchByReference(
        bookQuery: String,
        chapter: Int,
        verseStart: Int?,
        verseEnd: Int?,
    ): List<Verse> = withContext(ioDispatcher) {
        val language = resolveSelectedLanguageCode()

        // Resolve book: try exact name, then prefix, then abbreviation
        val book = bibleDao.getBookByName(bookQuery)
            ?: bibleDao.getBookByNamePrefix(bookQuery)
            ?: bibleDao.getBookByAbbreviation(bookQuery)
            ?: return@withContext emptyList()

        when {
            verseStart != null && verseEnd != null && verseEnd > verseStart -> {
                // Range: e.g., "John 3:16-18"
                (verseStart..verseEnd).mapNotNull { v ->
                    bibleDao.getExactVerse(book.id, chapter, v, language)?.let { entity ->
                        Verse(
                            bookId = entity.bookId,
                            bookName = book.name,
                            chapter = entity.chapter,
                            verseNumber = entity.verse,
                            text = entity.text,
                        )
                    }
                }
            }
            verseStart != null -> {
                // Single verse: e.g., "John 3:16"
                val entity = bibleDao.getExactVerse(book.id, chapter, verseStart, language)
                    ?: return@withContext emptyList()
                listOf(
                    Verse(
                        bookId = entity.bookId,
                        bookName = book.name,
                        chapter = entity.chapter,
                        verseNumber = entity.verse,
                        text = entity.text,
                    ),
                )
            }
            else -> {
                // Whole chapter: e.g., "John 3"
                bibleDao.getVersesForChapter(book.id, chapter, language).map { entity ->
                    Verse(
                        bookId = entity.bookId,
                        bookName = book.name,
                        chapter = entity.chapter,
                        verseNumber = entity.verse,
                        text = entity.text,
                    )
                }
            }
        }
    }

    override suspend fun getCrossReferences(
        bookId: Int,
        chapter: Int,
        verse: Int,
        limit: Int,
    ): List<BibleCrossReference> = withContext(ioDispatcher) {
        val bookNames = bibleDao.getAllBooks().first().associate { it.id to it.name }
        bibleDao.getCrossReferences(bookId, chapter, verse, limit).map { reference ->
            BibleCrossReference(
                fromBookId = reference.fromBookId,
                fromChapter = reference.fromChapter,
                fromVerse = reference.fromVerse,
                toBookId = reference.toBookId,
                toBookName = bookNames[reference.toBookId].orEmpty(),
                toChapter = reference.toChapter,
                toVerseStart = reference.toVerseStart,
                toVerseEnd = reference.toVerseEnd,
                votes = reference.votes,
            )
        }
    }

    override suspend fun toggleBookmark(bookId: Int, chapter: Int, verse: Int) = withContext(ioDispatcher) {
        val existing = bookmarkDao.getBookmark(bookId, chapter, verse)
        if (existing != null) {
            bookmarkDao.deleteBookmark(existing)
        } else {
            bookmarkDao.insertBookmark(
                BookmarkEntity(bookId = bookId, chapter = chapter, verse = verse)
            )
        }
    }

    override suspend fun getChapterCount(bookId: Int): Int = withContext(ioDispatcher) {
        val selected = resolveSelectedLanguageCode()
        bibleDao.getChapterCount(bookId, selected)?.let { return@withContext it }

        if (selected != ENGLISH_LANGUAGE_CODE) {
            bibleDao.getChapterCount(bookId, ENGLISH_LANGUAGE_CODE)?.let { return@withContext it }
        }

        if (selected != LATIN_LANGUAGE_CODE) {
            bibleDao.getChapterCount(bookId, LATIN_LANGUAGE_CODE)?.let { return@withContext it }
        }

        return@withContext 1
    }

    override suspend fun getAvailableLanguages(): List<BibleLanguage> = withContext(ioDispatcher) {
        return@withContext bibleDao.getAvailableVerseLanguages().map { code ->
            BibleLanguage(
                code = code,
                displayName = languageDisplayName(code),
            )
        }
    }

    override suspend fun getSelectedLanguageCode(): String = withContext(ioDispatcher) {
        resolveSelectedLanguageCode()
    }

    override suspend fun setSelectedLanguageCode(languageCode: String) = withContext(ioDispatcher) {
        val normalized = languageCode.lowercase()
        val available = bibleDao.getAvailableVerseLanguages().map { it.lowercase() }
        if (available.contains(normalized)) {
            bootstrapPreferences.setPreferredBibleLanguage(normalized)
            chapterCache.evictAll()
        }
    }

    private suspend fun resolveSelectedLanguageCode(): String {
        val available = bibleDao.getAvailableVerseLanguages().map { it.lowercase() }
        if (available.isEmpty()) return ENGLISH_LANGUAGE_CODE

        val stored = bootstrapPreferences.getPreferredBibleLanguage()?.lowercase()
        if (stored != null && available.contains(stored)) return stored

        val deviceLanguage = Locale.getDefault().language.lowercase()
        val resolved = when {
            deviceLanguage.startsWith("la") && available.contains(LATIN_LANGUAGE_CODE) -> LATIN_LANGUAGE_CODE
            available.contains(deviceLanguage) -> deviceLanguage
            available.contains(ENGLISH_LANGUAGE_CODE) -> ENGLISH_LANGUAGE_CODE
            available.contains(LATIN_LANGUAGE_CODE) -> LATIN_LANGUAGE_CODE
            else -> available.first()
        }

        bootstrapPreferences.setPreferredBibleLanguage(resolved)
        return resolved
    }

    // ── Reading preferences ──

    override suspend fun getReadingTheme(): String? = withContext(ioDispatcher) {
        bootstrapPreferences.getReadingTheme()
    }

    override suspend fun setReadingTheme(themeId: String) = withContext(ioDispatcher) {
        bootstrapPreferences.setReadingTheme(themeId)
    }

    override suspend fun getReadingMode(): String? = withContext(ioDispatcher) {
        bootstrapPreferences.getReadingMode()
    }

    override suspend fun setReadingMode(mode: String) = withContext(ioDispatcher) {
        bootstrapPreferences.setReadingMode(mode)
    }

    // ── Reading history ──

    override suspend fun recordReadingPosition(bookId: Int, chapter: Int, verse: Int) = withContext(ioDispatcher) {
        readingHistoryDao.insertHistory(
            ReadingHistoryEntity(
                bookId = bookId,
                chapter = chapter,
                lastVerse = verse,
            ),
        )
    }

    override fun getLastReadPosition(): Flow<ReadingPosition?> {
        return readingHistoryDao.getLastRead().map { entity ->
            entity?.let {
                ReadingPosition(
                    bookId = it.bookId,
                    chapter = it.chapter,
                    lastVerse = it.lastVerse,
                    timestamp = it.timestamp,
                )
            }
        }
    }

    private fun languageDisplayName(code: String): String {
        return when (code.lowercase()) {
            ENGLISH_LANGUAGE_CODE -> "English"
            LATIN_LANGUAGE_CODE -> "Latin"
            else -> code.replaceFirstChar { it.uppercase() }
        }
    }

    private companion object {
        const val ENGLISH_LANGUAGE_CODE = "en"
        const val LATIN_LANGUAGE_CODE = "la"
    }
}
