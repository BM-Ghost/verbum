package com.verbum.feature.bible.data.seed

import android.content.Context
import androidx.core.text.HtmlCompat
import com.verbum.core.common.dispatcher.Dispatcher
import com.verbum.core.common.dispatcher.VerbumDispatcher
import com.verbum.core.common.preferences.BootstrapPreferences
import com.verbum.core.database.dao.BibleDao
import com.verbum.core.database.entity.BibleBookEntity
import com.verbum.core.database.entity.BibleVerseEntity
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
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val seedMutex = Mutex()

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

                // PHASE 1: Insert verses (critical - must complete)
                try {
                    seedLatinVulsearchIfNeeded(books)
                } catch (error: Throwable) {
                    Timber.e(error, "Latin verse seeding failed")
                }

                try {
                    seedEnglishPg1581IfNeeded(books)
                } catch (error: Throwable) {
                    Timber.e(error, "English verse seeding failed")
                }

                // Force a count to ensure verses were actually inserted
                val latinVerseCount = bibleDao.countVerses(LATIN_LANGUAGE_CODE)
                val englishVerseCount = bibleDao.countVerses(ENGLISH_LANGUAGE_CODE)
                Timber.i("Verse count check: latin=%d english=%d", latinVerseCount, englishVerseCount)

                // Only proceed with metadata updates if we have verses
                if (latinVerseCount < 100 && englishVerseCount < 100) {
                    Timber.w(
                        "Bible seed produced insufficient verses (latin=%d, english=%d). Will retry on next launch.",
                        latinVerseCount,
                        englishVerseCount,
                    )
                    diagnosticsTracker.markSeedingFailed(
                        IllegalStateException(
                            "Insufficient verses after seeding (latin=$latinVerseCount, english=$englishVerseCount)"
                        )
                    )
                    return@withLock
                }

                Timber.i("Bible seed validated: latin=%d english=%d", latinVerseCount, englishVerseCount)

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
        val rows = context.assets.open("bible/source/etc/books.csv").bufferedReader().useLines { sequence ->
            sequence
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toList()
        }

        return rows.mapIndexedNotNull { _, row ->
            val parts = row.split(',')
            val abbr = parts[0].trim().uppercase()
            val testament = parts.getOrNull(1)?.trim()?.uppercase().orEmpty()
            if (testament != "OT" && testament != "NT") return@mapIndexedNotNull null

            BookMeta(
                id = 0,
                abbreviation = abbr,
                name = CANONICAL_BOOK_NAMES[abbr] ?: abbr,
                testament = testament,
                orderIndex = 0,
            )
        }.mapIndexed { index, meta ->
            meta.copy(id = index + 1, orderIndex = index + 1)
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
