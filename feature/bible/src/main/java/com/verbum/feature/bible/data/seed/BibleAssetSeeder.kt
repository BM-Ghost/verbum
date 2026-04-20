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

@Singleton
class BibleAssetSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bibleDao: BibleDao,
    private val bootstrapPreferences: BootstrapPreferences,
    @Dispatcher(VerbumDispatcher.IO) private val ioDispatcher: CoroutineDispatcher,
) {

    private val seedMutex = Mutex()

    suspend fun ensureSeeded() = withContext(ioDispatcher) {
        seedMutex.withLock {
            val books = loadBookMetadata()
            if (bibleDao.countBooks() == 0) {
                bibleDao.insertBooks(books.map { it.toEntity(totalChapters = 0) })
            }

            seedLatinVulsearchIfNeeded(books)
            seedEnglishPg1581IfNeeded(books)

            val availableLanguages = bibleDao.getAvailableVerseLanguages()
            if (availableLanguages.isEmpty()) {
                return@withLock
            }

            val updatedBooks = books.map { meta ->
                val totalChapters = availableLanguages
                    .mapNotNull { language -> bibleDao.getChapterCount(meta.id, language) }
                    .maxOrNull()
                    ?: 1
                meta.toEntity(totalChapters = totalChapters)
            }

            bibleDao.insertBooks(updatedBooks)
            bootstrapPreferences.markBiblePreloaded()
            initializePreferredLanguage(availableLanguages)
        }
    }

    private suspend fun seedLatinVulsearchIfNeeded(books: List<BookMeta>) {
        if (bibleDao.countVerses(LATIN_LANGUAGE_CODE) > 0) return

        val bibleDir = "bible/source/vulsearch_vulgate"
        val fileNames = context.assets.list(bibleDir).orEmpty().sorted()
        if (fileNames.isEmpty()) return

        for (fileName in fileNames) {
            if (!fileName.endsWith(".yaml", ignoreCase = true)) continue

            val abbreviation = fileName.substringBeforeLast('.').uppercase()
            val book = books.firstOrNull { it.abbreviation == abbreviation } ?: continue
            val assetPath = "$bibleDir/$fileName"

            context.assets.open(assetPath).bufferedReader().use { reader ->
                var currentChapter = 0
                val batch = ArrayList<BibleVerseEntity>(1024)

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

                    if (batch.size >= INSERT_BATCH_SIZE) {
                        bibleDao.insertVerses(batch.toList())
                        batch.clear()
                    }
                }

                if (batch.isNotEmpty()) {
                    bibleDao.insertVerses(batch)
                }
            }
        }
    }

    private suspend fun seedEnglishPg1581IfNeeded(books: List<BookMeta>) {
        if (bibleDao.countVerses(ENGLISH_LANGUAGE_CODE) > 0) return

        val sourcePath = "bible/source/pg1581/pg1581-images.html.utf8"
        val mappingPath = "bible/source/pg1581/src_pg1581.yaml"
        val sourceExists = runCatching { context.assets.open(sourcePath).close(); true }.getOrElse { false }
        val mappingExists = runCatching { context.assets.open(mappingPath).close(); true }.getOrElse { false }
        if (!sourceExists || !mappingExists) return

        val tagToUsfm = loadPg1581BookMap(mappingPath)
        val bookIdByAbbreviation = books.associate { it.abbreviation to it.id }

        var currentBookId: Int? = null
        val batch = ArrayList<BibleVerseEntity>(INSERT_BATCH_SIZE)
        // Paragraph accumulation: pg1581 HTML wraps verse <p> content across multiple lines.
        val paraBuffer = StringBuilder()
        var inVersePara = false

        context.assets.open(sourcePath).bufferedReader().use { reader ->
            while (true) {
                val rawLine = reader.readLine() ?: break
                val line = rawLine.trim()
                if (line.isBlank()) continue

                // Start of a verse paragraph — detected before accumulation
                if (!inVersePara && VERSE_PARA_START_REGEX.containsMatchIn(line)) {
                    inVersePara = true
                    paraBuffer.clear()
                }

                if (inVersePara) {
                    if (paraBuffer.isNotEmpty()) paraBuffer.append(' ')
                    paraBuffer.append(line)

                    // Only process once the closing </p> tag is present
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
                                    if (batch.size >= INSERT_BATCH_SIZE) {
                                        bibleDao.insertVerses(batch.toList())
                                        batch.clear()
                                    }
                                }
                            }
                        }
                        inVersePara = false
                        paraBuffer.clear()
                    }
                    // Still accumulating — skip header detection for this line
                    continue
                }

                // Book header (only checked when not accumulating a verse paragraph)
                val bookMatch = BOOK_HEADER_REGEX.find(line)
                if (bookMatch != null) {
                    val tag = bookMatch.groupValues[1].uppercase()
                    currentBookId = tagToUsfm[tag]?.let { bookIdByAbbreviation[it] }
                }
            }
        }

        if (batch.isNotEmpty()) {
            bibleDao.insertVerses(batch)
        }
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

        return rows.mapIndexed { index, row ->
            val parts = row.split(',')
            val abbr = parts[0].trim().uppercase()
            val testament = parts.getOrNull(1)?.trim().orEmpty()
            BookMeta(
                id = index + 1,
                abbreviation = abbr,
                name = CANONICAL_BOOK_NAMES[abbr] ?: abbr,
                testament = if (testament == "NT") "NT" else "OT",
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
        const val ENGLISH_LANGUAGE_CODE = "en"
        const val LATIN_LANGUAGE_CODE = "la"

        val CHAPTER_REGEX = Regex("^\\s*c:(\\d+):")
        val VERSE_REGEX = Regex("^\\s*v:(\\d+):\\s*(.+)$")
        val VS_MARKUP_REGEX = Regex("\\{VS:[^}]+}")
        val BOOK_HEADER_REGEX = Regex("<h3 class=\"nobreak\" id=\"([A-Z0-9_]+)\">", RegexOption.IGNORE_CASE)
        val CHAPTER_HEADER_REGEX = Regex("<h4>([^<]+)</h\\d>", RegexOption.IGNORE_CASE)
        val VERSE_PARA_START_REGEX = Regex("<p>\\d+:\\d+\\.", RegexOption.IGNORE_CASE)
        val HTML_VERSE_REGEX = Regex("<p>(\\d+):(\\d+)\\.\\s*(.+?)</p>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
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
            "MAN" to "Prayer of Manasses",
            "1ES" to "3 Esdras",
            "2ES" to "4 Esdras",
        )
    }
}
