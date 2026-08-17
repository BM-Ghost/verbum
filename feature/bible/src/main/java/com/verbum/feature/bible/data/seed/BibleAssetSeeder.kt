package com.verbum.feature.bible.data.seed

import android.content.Context
import androidx.core.text.HtmlCompat
import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.core.common.preferences.BootstrapPreferences
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.entity.BibleBookEntity
import com.verbum.core.database.entity.BibleVerseEntity
import com.verbum.core.database.entity.BibleCrossReferenceEntity
import com.verbum.core.network.api.ScrollmapperApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class BibleAssetSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bibleDao: BibleDao,
    private val diagnosticsTracker: BibleDiagnosticsTracker,
    private val bootstrapPreferences: BootstrapPreferences,
    private val scrollmapperApi: ScrollmapperApi,
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val seedMutex = Mutex()

    suspend fun refreshDrcFromOnline(): Result<Int> = withContext(ioDispatcher) {
        seedMutex.withLock {
            runCatching {
                val csv = scrollmapperApi.downloadDrc().use { it.string() }
                val books = loadBookMetadata()
                val bookIdBySourceName = SOURCE_BOOK_TO_ABBREVIATION.mapValues { (_, abbreviation) ->
                    books.first { it.abbreviation == abbreviation }.id
                }
                val verses = parseCsvRows(csv).drop(1).mapNotNull { row ->
                    if (row.size < 4) return@mapNotNull null
                    val bookId = bookIdBySourceName[row[0].trim()] ?: return@mapNotNull null
                    val chapter = row[1].toIntOrNull() ?: return@mapNotNull null
                    val verse = row[2].toIntOrNull() ?: return@mapNotNull null
                    val text = row[3].replace(Regex("\\s+"), " ").trim()
                    if (chapter <= 0 || verse <= 0 || text.isBlank()) return@mapNotNull null
                    BibleVerseEntity(ENGLISH_LANGUAGE_CODE, bookId, chapter, verse, text)
                }
                check(verses.size >= MIN_REQUIRED_VERSE_COUNT) {
                    "Online DRC payload is incomplete (${verses.size} verses)"
                }

                verses.chunked(LARGE_INSERT_BATCH_SIZE).forEach { bibleDao.insertVerses(it) }
                val updatedBooks = books.map { meta ->
                    meta.toEntity(bibleDao.getChapterCount(meta.id, ENGLISH_LANGUAGE_CODE) ?: 1)
                }
                bibleDao.updateBooks(updatedBooks)
                bootstrapPreferences.setBibleAssetVersion(CURRENT_BIBLE_ASSET_VERSION)
                bootstrapPreferences.markBiblePreloaded()
                verses.size
            }
        }
    }

    suspend fun ensureSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            diagnosticsTracker.markSeedingStarted()
            try {
                val books = loadBookMetadata()
                val canonicalBookIds = books.map { it.id }

                bibleDao.deleteVersesForUnknownBooks(canonicalBookIds)
                bibleDao.deleteBooksNotIn(canonicalBookIds)

                if (bibleDao.countBooks() == 0) {
                    bibleDao.insertBooks(books.map { it.toEntity(totalChapters = 0) })
                } else {
                    bibleDao.updateBooks(books.map { it.toEntity(totalChapters = 0) })
                }

                val assetVersion = bootstrapPreferences.getBibleAssetVersion()
                if (assetVersion != CURRENT_BIBLE_ASSET_VERSION) {
                    bibleDao.deleteVersesByLanguage(ENGLISH_LANGUAGE_CODE)
                    bibleDao.deleteVersesByLanguage(LATIN_LANGUAGE_CODE)
                }

                // PHASE 1: Insert verses (critical - must complete)
                try {
                    seedDrcIfNeeded(books, assetVersion)
                    seedCrossReferencesIfNeeded(books, assetVersion)
                } catch (error: Throwable) {
                    Timber.e(error, "Bible asset seeding failed")
                }

                // Force a count to ensure verses were actually inserted
                val englishVerseCount = bibleDao.countVerses(ENGLISH_LANGUAGE_CODE)
                Timber.i("Verse count check: english=%d", englishVerseCount)

                // Only proceed with metadata updates if we have verses
                if (englishVerseCount < MIN_REQUIRED_VERSE_COUNT) {
                    Timber.w(
                        "Bible seed produced insufficient verses (english=%d). Will retry on next launch.",
                        englishVerseCount,
                    )
                    diagnosticsTracker.markSeedingFailed(
                        IllegalStateException("Insufficient DRC verses after seeding (english=$englishVerseCount)")
                    )
                    return@withLock
                }

                Timber.i("Bible seed validated: english=%d", englishVerseCount)

                // PHASE 2: Update metadata (non-critical)
                try {
                    val availableLanguages = bibleDao.getAvailableVerseLanguages()
                    if (availableLanguages.isEmpty()) {
                        Timber.e("No verse languages found after seeding")
                        return@withLock
                    }

                    val updatedBooks = books.map { meta ->
                        val totalChapters = availableLanguages
                            .mapNotNull { language -> bibleDao.getChapterCount(meta.id, language) }
                            .maxOrNull()
                            ?: 1
                        meta.toEntity(totalChapters = totalChapters)
                    }

                    bibleDao.updateBooks(updatedBooks)
                    bootstrapPreferences.markBiblePreloaded()
                    bootstrapPreferences.setBibleAssetVersion(CURRENT_BIBLE_ASSET_VERSION)
                    initializePreferredLanguage(availableLanguages)

                    val snapshot = buildDiagnosticsSnapshot(
                        books = books,
                        availableLanguages = availableLanguages,
                        updatedBooks = updatedBooks,
                    )
                    diagnosticsTracker.markSeedingSucceeded(snapshot)
                } catch (metadataError: Throwable) {
                    Timber.e(metadataError, "Bible metadata update failed, but verses are seeded")
                    // Still mark as succeeded since verses are the critical part
                    val availableLanguages = bibleDao.getAvailableVerseLanguages()
                    val snapshot = buildDiagnosticsSnapshot(
                        books = books,
                        availableLanguages = availableLanguages,
                        updatedBooks = books.map { it.toEntity(totalChapters = 0) },
                    )
                    diagnosticsTracker.markSeedingSucceeded(snapshot)
                }
            } catch (error: Throwable) {
                diagnosticsTracker.markSeedingFailed(error)
                Timber.e(error, "Bible seeding failed; continuing without crash")
            }
        }
    }

    private suspend fun buildDiagnosticsSnapshot(
        books: List<BookMeta>,
        availableLanguages: List<String>,
        updatedBooks: List<BibleBookEntity>,
    ): BibleDiagnosticsSnapshot {
        val languageVerseCounts = linkedMapOf<String, Int>()
        val missingBooksByLanguage = linkedMapOf<String, List<String>>()
        val partialBooksByLanguage = linkedMapOf<String, List<String>>()
        val missingChapterCountByLanguage = linkedMapOf<String, Int>()
        val expectedByBookId = updatedBooks.associateBy { it.id }

        for (language in availableLanguages) {
            languageVerseCounts[language] = bibleDao.countVerses(language)

            val missingBooks = mutableListOf<String>()
            val partialBooks = mutableListOf<String>()
            var missingChapters = 0

            for (book in books) {
                val expectedTotal = expectedByBookId[book.id]?.totalChapters ?: 1
                val chapterCount = bibleDao.getChapterCount(book.id, language) ?: 0
                when {
                    chapterCount <= 0 -> missingBooks += book.abbreviation
                    chapterCount < expectedTotal -> {
                        val deficit = expectedTotal - chapterCount
                        partialBooks += "${book.abbreviation}(-$deficit)"
                        missingChapters += deficit
                    }
                }
            }

            missingBooksByLanguage[language] = missingBooks
            partialBooksByLanguage[language] = partialBooks
            missingChapterCountByLanguage[language] = missingChapters
        }

        return BibleDiagnosticsSnapshot(
            generatedAtMs = System.currentTimeMillis(),
            totalBooks = books.size,
            languageVerseCounts = languageVerseCounts,
            missingBooksByLanguage = missingBooksByLanguage,
            partialBooksByLanguage = partialBooksByLanguage,
            missingChapterCountByLanguage = missingChapterCountByLanguage,
        )
    }

    private suspend fun shouldReseedLanguage(
        languageCode: String,
        books: List<BookMeta>,
        existingVerseCount: Int,
    ): Boolean {
        if (existingVerseCount < MIN_REQUIRED_VERSE_COUNT) return true

        val missingBookCount = books.count { book ->
            (bibleDao.getChapterCount(book.id, languageCode) ?: 0) <= 0
        }

        if (missingBookCount > 0) {
            Timber.w(
                "Language %s has %d missing books; forcing reseed.",
                languageCode,
                missingBookCount,
            )
            return true
        }

        return false
    }

    private suspend fun seedDrcIfNeeded(books: List<BookMeta>, assetVersion: Int) {
        val existing = bibleDao.countVerses(ENGLISH_LANGUAGE_CODE)
        if (assetVersion == CURRENT_BIBLE_ASSET_VERSION && !shouldReseedLanguage(ENGLISH_LANGUAGE_CODE, books, existing)) {
            Timber.i("DRC verses already seeded and complete (%d verses)", existing)
            return
        }

        val bookIdBySourceName = SOURCE_BOOK_TO_ABBREVIATION.mapValues { (_, abbreviation) ->
            books.first { it.abbreviation == abbreviation }.id
        }
        val batch = ArrayList<BibleVerseEntity>(LARGE_INSERT_BATCH_SIZE)
        var totalInserted = 0

        context.assets.open(DRC_ASSET_PATH).bufferedReader().use { reader ->
            parseCsvRows(reader.readText()).drop(1).forEach { row ->
                if (row.size < 4) return@forEach
                val bookId = bookIdBySourceName[row[0].trim()] ?: return@forEach
                val chapter = row[1].toIntOrNull() ?: return@forEach
                val verse = row[2].toIntOrNull() ?: return@forEach
                val text = row[3].replace(Regex("\\s+"), " ").trim()
                if (chapter <= 0 || verse <= 0 || text.isBlank()) return@forEach

                batch += BibleVerseEntity(
                    languageCode = ENGLISH_LANGUAGE_CODE,
                    bookId = bookId,
                    chapter = chapter,
                    verse = verse,
                    text = text,
                )
                if (batch.size >= LARGE_INSERT_BATCH_SIZE) {
                    bibleDao.insertVerses(batch.toList())
                    totalInserted += batch.size
                    batch.clear()
                }
            }
        }

        if (batch.isNotEmpty()) {
            bibleDao.insertVerses(batch)
            totalInserted += batch.size
        }
        Timber.i("DRC seeding complete. Inserted=%d verified=%d", totalInserted, bibleDao.countVerses(ENGLISH_LANGUAGE_CODE))
    }

    private fun parseCsvRows(content: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var index = 0

        fun finishField() {
            row += field.toString()
            field.clear()
        }
        fun finishRow() {
            finishField()
            if (row.any { it.isNotBlank() }) rows += row.toList()
            row.clear()
        }

        while (index < content.length) {
            val character = content[index]
            when {
                character == '"' && quoted && index + 1 < content.length && content[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }
                character == '"' -> quoted = !quoted
                character == ',' && !quoted -> finishField()
                (character == '\n' || character == '\r') && !quoted -> {
                    if (character == '\r' && index + 1 < content.length && content[index + 1] == '\n') index++
                    finishRow()
                }
                else -> field.append(character)
            }
            index++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) finishRow()
        return rows
    }

    private suspend fun seedCrossReferencesIfNeeded(books: List<BookMeta>, assetVersion: Int) {
        val existing = bibleDao.countCrossReferences()
        if (assetVersion == CURRENT_BIBLE_ASSET_VERSION && existing > 0) {
            Timber.i("Bible cross-references already indexed (%d rows)", existing)
            return
        }

        bibleDao.deleteCrossReferences()
        val bookIdByAlias = CROSS_REFERENCE_BOOK_ALIASES.mapValues { (_, abbreviation) ->
            books.first { it.abbreviation == abbreviation }.id
        }
        val batch = ArrayList<BibleCrossReferenceEntity>(LARGE_INSERT_BATCH_SIZE)
        var totalInserted = 0

        context.assets.open(CROSS_REFERENCE_ASSET_PATH).bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val columns = line.trim().split(Regex("\\s+"))
                if (columns.size < 3) return@forEach

                val from = parseCrossReference(columns[0], bookIdByAlias) ?: return@forEach
                val to = parseCrossReference(columns[1], bookIdByAlias) ?: return@forEach
                val votes = columns[2].toIntOrNull() ?: return@forEach

                batch += BibleCrossReferenceEntity(
                    fromBookId = from.bookId,
                    fromChapter = from.chapter,
                    fromVerse = from.verse,
                    toBookId = to.bookId,
                    toChapter = to.chapter,
                    toVerseStart = to.verse,
                    toVerseEnd = to.verseEnd,
                    votes = votes,
                )
                if (batch.size >= LARGE_INSERT_BATCH_SIZE) {
                    bibleDao.insertCrossReferences(batch.toList())
                    totalInserted += batch.size
                    batch.clear()
                }
            }
        }

        if (batch.isNotEmpty()) {
            bibleDao.insertCrossReferences(batch)
            totalInserted += batch.size
        }
        Timber.i("Bible cross-reference indexing complete. Inserted=%d", totalInserted)
    }

    private fun parseCrossReference(value: String, bookIdByAlias: Map<String, Int>): ParsedCrossReference? {
        val match = CROSS_REFERENCE_REGEX.matchEntire(value) ?: return null
        val bookId = bookIdByAlias[match.groupValues[1]] ?: return null
        val chapter = match.groupValues[2].toIntOrNull() ?: return null
        val verse = match.groupValues[3].toIntOrNull() ?: return null
        val endBook = match.groupValues[4].takeIf { it.isNotBlank() }
        if (endBook != null && endBook != match.groupValues[1]) return null
        val endChapter = match.groupValues[5].toIntOrNull()
        if (endChapter != null && endChapter != chapter) return null
        val verseEnd = match.groupValues[6].toIntOrNull() ?: verse
        if (chapter <= 0 || verse <= 0 || verseEnd < verse) return null
        return ParsedCrossReference(bookId, chapter, verse, verseEnd)
    }

    private data class ParsedCrossReference(
        val bookId: Int,
        val chapter: Int,
        val verse: Int,
        val verseEnd: Int,
    )

    private suspend fun seedLatinVulsearchIfNeeded(books: List<BookMeta>) {
        val existing = bibleDao.countVerses(LATIN_LANGUAGE_CODE)
        if (!shouldReseedLanguage(LATIN_LANGUAGE_CODE, books, existing)) {
            Timber.i("Latin verses already seeded and complete (%d verses)", existing)
            return
        }
        if (existing > 0) {
            Timber.w("Latin seed incomplete (%d). Clearing and reseeding.", existing)
            bibleDao.deleteVersesByLanguage(LATIN_LANGUAGE_CODE)
        }

        Timber.i("Starting Latin Vulgate seeding...")
        val bibleDir = "bible/source/vulsearch_vulgate"
        val fileNames = context.assets.list(bibleDir).orEmpty().sorted()
        if (fileNames.isEmpty()) {
            Timber.e("No Latin source files found")
            return
        }

        var totalInserted = 0
        for (fileName in fileNames) {
            if (!fileName.endsWith(".yaml", ignoreCase = true)) continue

            val abbreviation = fileName.substringBeforeLast('.').uppercase()
            val book = books.firstOrNull { it.abbreviation == abbreviation } ?: continue
            val assetPath = "$bibleDir/$fileName"

            context.assets.open(assetPath).bufferedReader().use { reader ->
                var currentChapter = 0
                val batch = ArrayList<BibleVerseEntity>(LARGE_INSERT_BATCH_SIZE)

                while (true) {
                    val line = reader.readLine() ?: break
                    val chapterMatch = CHAPTER_REGEX.find(line)
                    if (chapterMatch != null) {
                        currentChapter = chapterMatch.groupValues[1].toInt()
                        continue
                    }

                    val verseMatch = VERSE_REGEX.find(line) ?: continue
                    if (currentChapter <= 0) continue

                    val verse = verseMatch.groupValues[1].toInt()
                    val rawPayload = verseMatch.groupValues[2]
                    val verseText = extractVulsearchVerseText(rawPayload)
                    if (verseText.isBlank()) continue

                    batch.add(
                        BibleVerseEntity(
                            languageCode = LATIN_LANGUAGE_CODE,
                            bookId = book.id,
                            chapter = currentChapter,
                            verse = verse,
                            text = verseText,
                        )
                    )

                    if (batch.size >= LARGE_INSERT_BATCH_SIZE) {
                        bibleDao.insertVerses(batch.toList())
                        totalInserted += batch.size
                        Timber.d("Latin insert batch: %d verses (total: %d)", batch.size, totalInserted)
                        batch.clear()
                    }
                }

                if (batch.isNotEmpty()) {
                    bibleDao.insertVerses(batch)
                    totalInserted += batch.size
                    Timber.d("Latin final batch: %d verses (total: %d)", batch.size, totalInserted)
                }
            }
        }

        val verifyCount = bibleDao.countVerses(LATIN_LANGUAGE_CODE)
        Timber.i("Latin seeding complete. Total inserted: %d, verified in DB: %d", totalInserted, verifyCount)
    }

    private suspend fun seedEnglishPg1581IfNeeded(books: List<BookMeta>) {
        val existing = bibleDao.countVerses(ENGLISH_LANGUAGE_CODE)
        if (!shouldReseedLanguage(ENGLISH_LANGUAGE_CODE, books, existing)) {
            Timber.i("English verses already seeded and complete (%d verses)", existing)
            return
        }
        if (existing > 0) {
            Timber.w("English seed incomplete (%d). Clearing and reseeding.", existing)
            bibleDao.deleteVersesByLanguage(ENGLISH_LANGUAGE_CODE)
        }

        Timber.i("Starting English pg1581 seeding...")
        val sourcePath = "bible/source/pg1581/pg1581-images.html.utf8"
        val mappingPath = "bible/source/pg1581/src_pg1581.yaml"
        val sourceExists = runCatching { context.assets.open(sourcePath).close(); true }.getOrElse { false }
        val mappingExists = runCatching { context.assets.open(mappingPath).close(); true }.getOrElse { false }
        if (!sourceExists || !mappingExists) {
            Timber.e("English source files not found (sourceExists=$sourceExists, mappingExists=$mappingExists)")
            return
        }

        val tagToUsfm = loadPg1581BookMap(mappingPath)
        val bookIdByAbbreviation = books.associate { it.abbreviation to it.id }

        var currentBookId: Int? = null
        val batch = ArrayList<BibleVerseEntity>(LARGE_INSERT_BATCH_SIZE)
        val paraBuffer = StringBuilder()
        var inVersePara = false
        var totalInserted = 0

        context.assets.open(sourcePath).bufferedReader().use { reader ->
            while (true) {
                val rawLine = reader.readLine() ?: break
                val line = rawLine.trim()
                if (line.isBlank()) continue

                val bookMatch = BOOK_HEADER_REGEX.find(line)
                if (bookMatch != null) {
                    val tag = bookMatch.groupValues[1].uppercase()
                    currentBookId = tagToUsfm[tag]?.let { bookIdByAbbreviation[it] }
                }

                if (!inVersePara && VERSE_PARA_START_REGEX.containsMatchIn(line)) {
                    inVersePara = true
                    paraBuffer.clear()
                }

                if (inVersePara) {
                    if (paraBuffer.isNotEmpty()) paraBuffer.append(' ')
                    paraBuffer.append(line)

                    if (line.contains("</p>", ignoreCase = true)) {
                        val fullPara = paraBuffer.toString()
                        val verseMatch = HTML_VERSE_REGEX.find(fullPara)
                        val bookId = currentBookId
                        if (verseMatch != null && bookId != null) {
                            val chapter = verseMatch.groupValues[1].toIntOrNull()
                            val verse = verseMatch.groupValues[2].toIntOrNull()
                            if (chapter != null && verse != null && chapter > 0 && verse > 0) {
                                val verseText = sanitizeHtmlVerse(verseMatch.groupValues[3])
                                if (verseText.isNotBlank()) {
                                    batch.add(
                                        BibleVerseEntity(
                                            languageCode = ENGLISH_LANGUAGE_CODE,
                                            bookId = bookId,
                                            chapter = chapter,
                                            verse = verse,
                                            text = verseText,
                                        )
                                    )
                                    if (batch.size >= LARGE_INSERT_BATCH_SIZE) {
                                        bibleDao.insertVerses(batch.toList())
                                        totalInserted += batch.size
                                        Timber.d("English insert batch: %d verses (total: %d)", batch.size, totalInserted)
                                        batch.clear()
                                    }
                                }
                            }
                        }
                        inVersePara = false
                        paraBuffer.clear()
                    }
                    continue
                }
            }
        }

        if (batch.isNotEmpty()) {
            bibleDao.insertVerses(batch)
            totalInserted += batch.size
            Timber.d("English final batch: %d verses (total: %d)", batch.size, totalInserted)
        }

        val verifyCount = bibleDao.countVerses(ENGLISH_LANGUAGE_CODE)
        Timber.i("English seeding complete. Total inserted: %d, verified in DB: %d", totalInserted, verifyCount)
    }

    private suspend fun initializePreferredLanguage(availableLanguages: List<String>) {
        val available = availableLanguages.map { it.lowercase() }
        val current = bootstrapPreferences.getPreferredBibleLanguage()?.lowercase()
        if (current != null && available.contains(current)) return

        val deviceLanguage = Locale.getDefault().language.lowercase()
        val resolved = when {
            deviceLanguage.startsWith("la") && available.contains(LATIN_LANGUAGE_CODE) -> LATIN_LANGUAGE_CODE
            available.contains(deviceLanguage) -> deviceLanguage
            available.contains(ENGLISH_LANGUAGE_CODE) -> ENGLISH_LANGUAGE_CODE
            available.contains(LATIN_LANGUAGE_CODE) -> LATIN_LANGUAGE_CODE
            else -> available.firstOrNull() ?: ENGLISH_LANGUAGE_CODE
        }

        bootstrapPreferences.setPreferredBibleLanguage(resolved)
    }

    private fun extractVulsearchVerseText(rawPayload: String): String {
        val normalized = rawPayload.trim().trim('\'', '"')
        val parts = normalized.split("|", limit = 4)
        val content = if (parts.size == 4) parts[3] else normalized.substringAfterLast('|', normalized)
        return content
            .replace(VS_MARKUP_REGEX, "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun sanitizeHtmlVerse(rawHtml: String): String {
        val noSuperscript = rawHtml.replace(SUPERSCRIPT_REGEX, "")
        return HtmlCompat.fromHtml(noSuperscript, HtmlCompat.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun loadPg1581BookMap(assetPath: String): Map<String, String> {
        val map = linkedMapOf<String, String>()
        var currentTag: String? = null

        context.assets.open(assetPath).bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed == "---") return@forEach

                if (!line.startsWith(" ") && trimmed.endsWith(":")) {
                    currentTag = trimmed.removeSuffix(":")
                    return@forEach
                }

                if (trimmed.startsWith("USFM_ID:")) {
                    val usfm = trimmed.substringAfter(':').trim().trim('\'', '"').uppercase()
                    val tag = currentTag
                    if (!tag.isNullOrBlank() && usfm.isNotBlank()) {
                        map[tag.uppercase()] = usfm
                    }
                }
            }
        }

        return map
    }

    private fun parseChapterHeader(header: String): Int {
        val normalized = sanitizeHtmlVerse(header)
        if (normalized.equals("PREFACE", ignoreCase = true)) return 0

        CHAPTER_DECIMAL_REGEX.find(normalized)?.let { match ->
            return match.groupValues[1].toIntOrNull() ?: 0
        }

        CHAPTER_ROMAN_REGEX.find(normalized)?.let { match ->
            return romanToInt(match.groupValues[1])
        }

        return 0
    }

    private fun romanToInt(roman: String): Int {
        val values = mapOf('I' to 1, 'V' to 5, 'X' to 10, 'L' to 50, 'C' to 100)
        var total = 0
        var previous = 0
        for (char in roman.uppercase().reversed()) {
            val value = values[char] ?: continue
            if (value < previous) {
                total -= value
            } else {
                total += value
                previous = value
            }
        }
        return total
    }

    private fun loadBookMetadata(): List<BookMeta> {
        return CANONICAL_BOOK_NAMES.entries.mapIndexed { index, (abbreviation, name) ->
            BookMeta(
                id = index + 1,
                abbreviation = abbreviation,
                name = name,
                testament = if (index < OLD_TESTAMENT_BOOK_COUNT) "OT" else "NT",
                orderIndex = index + 1,
            )
        }
    }

    private data class BookMeta(
        val id: Int,
        val abbreviation: String,
        val name: String,
        val testament: String,
        val orderIndex: Int,
    ) {
        fun toEntity(totalChapters: Int): BibleBookEntity {
            return BibleBookEntity(
                id = id,
                name = name,
                abbreviation = abbreviation,
                testament = testament,
                totalChapters = totalChapters,
                orderIndex = orderIndex,
            )
        }
    }

    private companion object {
        const val INSERT_BATCH_SIZE = 1000
        const val LARGE_INSERT_BATCH_SIZE = 5000  // Faster batch insertion to avoid Job cancellation
        const val MIN_REQUIRED_VERSE_COUNT = 30000
        const val CURRENT_BIBLE_ASSET_VERSION = 2025
        const val DRC_ASSET_PATH = "bible/source/scrollmapper/DRC.csv"
        const val CROSS_REFERENCE_ASSET_PATH = "bible/source/scrollmapper/cross_references.txt"
        const val OLD_TESTAMENT_BOOK_COUNT = 46
        const val ENGLISH_LANGUAGE_CODE = "en"
        const val LATIN_LANGUAGE_CODE = "la"

        val CHAPTER_REGEX = Regex("^\\s*c:(\\d+):")
        val VERSE_REGEX = Regex("^\\s*v:(\\d+):\\s*(.+)$")
        val VS_MARKUP_REGEX = Regex("\\{VS:[^}]+\\}")
        val BOOK_HEADER_REGEX = Regex("<h3[^>]*id=\"([A-Z0-9_]+)\"[^>]*>", RegexOption.IGNORE_CASE)
        val CHAPTER_HEADER_REGEX = Regex("<h4>([^<]+)</h\\d>", RegexOption.IGNORE_CASE)
        val VERSE_PARA_START_REGEX = Regex("<p[^>]*>\\s*\\d+:\\d+\\.", RegexOption.IGNORE_CASE)
        val HTML_VERSE_REGEX = Regex("<p[^>]*>\\s*(\\d+):(\\d+)\\.\\s*(.+?)</p>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val SUPERSCRIPT_REGEX = Regex("<sup[^>]*>.*?</sup>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
        val CHAPTER_DECIMAL_REGEX = Regex("Chapter\\s+(\\d+)", RegexOption.IGNORE_CASE)
        val CHAPTER_ROMAN_REGEX = Regex("CHAP\\.\\s*([IVXLCDM]+)\\.", RegexOption.IGNORE_CASE)
        val CROSS_REFERENCE_REGEX = Regex("^([A-Za-z0-9]+)\\.(\\d+)\\.(\\d+)(?:-([A-Za-z0-9]+)\\.(\\d+)\\.(\\d+))?$")

        val SOURCE_BOOK_TO_ABBREVIATION = mapOf(
            "Genesis" to "GEN", "Exodus" to "EXO", "Leviticus" to "LEV", "Numbers" to "NUM",
            "Deuteronomy" to "DEU", "Joshua" to "JOS", "Judges" to "JDG", "Ruth" to "RUT",
            "I Samuel" to "1SA", "II Samuel" to "2SA", "I Kings" to "1KI", "II Kings" to "2KI",
            "I Chronicles" to "1CH", "II Chronicles" to "2CH", "Ezra" to "EZR", "Nehemiah" to "NEH",
            "Tobit" to "TOB", "Judith" to "JDT", "Esther" to "ESG", "Job" to "JOB", "Psalms" to "PSA",
            "Proverbs" to "PRO", "Ecclesiastes" to "ECC", "Song of Solomon" to "SNG", "Wisdom" to "WIS",
            "Sirach" to "SIR", "Isaiah" to "ISA", "Jeremiah" to "JER", "Lamentations" to "LAM",
            "Baruch" to "BAR", "Ezekiel" to "EZK", "Daniel" to "DAG", "Hosea" to "HOS", "Joel" to "JOL",
            "Amos" to "AMO", "Obadiah" to "OBA", "Jonah" to "JON", "Micah" to "MIC", "Nahum" to "NAM",
            "Habakkuk" to "HAB", "Zephaniah" to "ZEP", "Haggai" to "HAG", "Zechariah" to "ZEC",
            "Malachi" to "MAL", "I Maccabees" to "1MA", "II Maccabees" to "2MA", "Matthew" to "MAT",
            "Mark" to "MRK", "Luke" to "LUK", "John" to "JHN", "Acts" to "ACT", "Romans" to "ROM",
            "I Corinthians" to "1CO", "II Corinthians" to "2CO", "Galatians" to "GAL", "Ephesians" to "EPH",
            "Philippians" to "PHP", "Colossians" to "COL", "I Thessalonians" to "1TH", "II Thessalonians" to "2TH",
            "I Timothy" to "1TI", "II Timothy" to "2TI", "Titus" to "TIT", "Philemon" to "PHM", "Hebrews" to "HEB",
            "James" to "JAS", "I Peter" to "1PE", "II Peter" to "2PE", "I John" to "1JN", "II John" to "2JN",
            "III John" to "3JN", "Jude" to "JUD", "Revelation of John" to "REV",
        )

        val CROSS_REFERENCE_BOOK_ALIASES = mapOf(
            "Gen" to "GEN", "Exo" to "EXO", "Lev" to "LEV", "Num" to "NUM", "Deut" to "DEU",
            "Josh" to "JOS", "Judg" to "JDG", "Ruth" to "RUT", "1Sam" to "1SA", "2Sam" to "2SA",
            "1Kgs" to "1KI", "2Kgs" to "2KI", "1Chr" to "1CH", "2Chr" to "2CH", "Ezra" to "EZR",
            "Neh" to "NEH", "Tob" to "TOB", "Jdt" to "JDT", "Esth" to "ESG", "Job" to "JOB",
            "Ps" to "PSA", "Prov" to "PRO", "Eccl" to "ECC", "Song" to "SNG", "Wis" to "WIS",
            "Sir" to "SIR", "Isa" to "ISA", "Jer" to "JER", "Lam" to "LAM", "Bar" to "BAR",
            "Ezek" to "EZK", "Dan" to "DAG", "Hos" to "HOS", "Joel" to "JOL", "Amos" to "AMO",
            "Obad" to "OBA", "Jonah" to "JON", "Mic" to "MIC", "Nah" to "NAM", "Hab" to "HAB",
            "Zeph" to "ZEP", "Hag" to "HAG", "Zech" to "ZEC", "Mal" to "MAL", "1Macc" to "1MA",
            "2Macc" to "2MA", "Matt" to "MAT", "Mark" to "MRK", "Luke" to "LUK", "John" to "JHN",
            "Acts" to "ACT", "Rom" to "ROM", "1Cor" to "1CO", "2Cor" to "2CO", "Gal" to "GAL",
            "Eph" to "EPH", "Phil" to "PHP", "Col" to "COL", "1Thess" to "1TH", "2Thess" to "2TH",
            "1Tim" to "1TI", "2Tim" to "2TI", "Titus" to "TIT", "Phlm" to "PHM", "Heb" to "HEB",
            "Jas" to "JAS", "1Pet" to "1PE", "2Pet" to "2PE", "1John" to "1JN", "2John" to "2JN",
            "3John" to "3JN", "Jude" to "JUD", "Rev" to "REV",
        )

        val CANONICAL_BOOK_NAMES = mapOf(
            "GEN" to "Genesis",
            "EXO" to "Exodus",
            "LEV" to "Leviticus",
            "NUM" to "Numbers",
            "DEU" to "Deuteronomy",
            "JOS" to "Josue",
            "JDG" to "Judges",
            "RUT" to "Ruth",
            "1SA" to "1 Samuel",
            "2SA" to "2 Samuel",
            "1KI" to "1 Kings",
            "2KI" to "2 Kings",
            "1CH" to "1 Chronicles",
            "2CH" to "2 Chronicles",
            "EZR" to "1 Esdras",
            "NEH" to "Nehemias",
            "TOB" to "Tobias",
            "JDT" to "Judith",
            "ESG" to "Esther",
            "JOB" to "Job",
            "PSA" to "Psalms",
            "PRO" to "Proverbs",
            "ECC" to "Ecclesiastes",
            "SNG" to "Canticle of Canticles",
            "WIS" to "Wisdom",
            "SIR" to "Ecclesiasticus",
            "ISA" to "Isaias",
            "JER" to "Jeremias",
            "LAM" to "Lamentations",
            "BAR" to "Baruch",
            "EZK" to "Ezechiel",
            "DAG" to "Daniel",
            "HOS" to "Osee",
            "JOL" to "Joel",
            "AMO" to "Amos",
            "OBA" to "Abdias",
            "JON" to "Jonas",
            "MIC" to "Micheas",
            "NAM" to "Nahum",
            "HAB" to "Habacuc",
            "ZEP" to "Sophonias",
            "HAG" to "Aggeus",
            "ZEC" to "Zacharias",
            "MAL" to "Malachias",
            "1MA" to "1 Machabees",
            "2MA" to "2 Machabees",
            "MAT" to "Matthew",
            "MRK" to "Mark",
            "LUK" to "Luke",
            "JHN" to "John",
            "ACT" to "Acts",
            "ROM" to "Romans",
            "1CO" to "1 Corinthians",
            "2CO" to "2 Corinthians",
            "GAL" to "Galatians",
            "EPH" to "Ephesians",
            "PHP" to "Philippians",
            "COL" to "Colossians",
            "1TH" to "1 Thessalonians",
            "2TH" to "2 Thessalonians",
            "1TI" to "1 Timothy",
            "2TI" to "2 Timothy",
            "TIT" to "Titus",
            "PHM" to "Philemon",
            "HEB" to "Hebrews",
            "JAS" to "James",
            "1PE" to "1 Peter",
            "2PE" to "2 Peter",
            "1JN" to "1 John",
            "2JN" to "2 John",
            "3JN" to "3 John",
            "JUD" to "Jude",
            "REV" to "Revelation",
        )
    }
}
