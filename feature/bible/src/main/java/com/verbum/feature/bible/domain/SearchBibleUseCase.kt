package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.Verse
import javax.inject.Inject
import kotlin.math.min

/**
 * Smart Bible search that understands:
 * - Scripture references: "John 3:16", "Gen 1", "1 Cor 13:4-7", "Ps 23"
 * - Concatenated: "Genesis1 2" → Genesis 1:2, "1cor13:4" → 1 Cor 13:4
 * - Fuzzy book names: "Genoasis 2 1" → Genesis 2:1, "Phillipians 4" → Philippians 4
 * - Word/phrase search: "love", "in the beginning"
 */
class SearchBibleUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(query: String): List<Verse> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        // First try multi-reference (comma-separated)
        val multiRef = parseMultiReference(trimmed)
        if (multiRef != null) {
            val allVerses = mutableListOf<Verse>()
            for (ref in multiRef.refs) {
                val results = repository.searchByReference(
                    bookQuery = ref.book,
                    chapter = ref.chapter,
                    verseStart = ref.verseStart,
                    verseEnd = ref.verseEnd,
                )
                allVerses.addAll(results)
            }
            if (allVerses.isNotEmpty()) return allVerses
        }

        // Then try single reference
        val ref = parseReference(trimmed)
        if (ref != null) {
            val results = repository.searchByReference(
                bookQuery = ref.book,
                chapter = ref.chapter,
                verseStart = ref.verseStart,
                verseEnd = ref.verseEnd,
            )
            if (results.isNotEmpty()) return results
        }

        return repository.searchVerses(trimmed)
    }

    /**
     * Parse multiple comma-separated references.
     * e.g., "John 3:16, Romans 8:28" → MultiScriptureRef with two references
     */
    private fun parseMultiReference(input: String): MultiScriptureRef? {
        val s = input.trim().replace(Regex("\\s+"), " ")
        
        // Split by comma to get individual references
        val parts = s.split(",").map { it.trim() }
        if (parts.size <= 1) return null
        
        val refs = mutableListOf<ScriptureRef>()
        for (part in parts) {
            val ref = parseReference(part)
            if (ref != null) {
                refs.add(ref)
            }
        }
        
        return if (refs.isNotEmpty()) MultiScriptureRef(refs) else null
    }

    /**
     * Parse a reference string. Handles:
     * - "John 3:16", "Gen 1", "1 Cor 13:4-7"
     * - "Genesis1 2"  → Genesis 1:2 (no space between book and chapter)
     * - "Genoasis 2 1" → Genesis 2:1 (fuzzy book name)
     * - "1cor13:4"    → 1 Corinthians 13:4
     * - Chapter:verse separator can be ':', ' ', or '.'
     */
    private fun parseReference(input: String): ScriptureRef? {
        val s = input.trim().replace(Regex("\\s+"), " ")

        // Group 1 = book  : optional leading digit + spaces + letters (+ optional second word)
        // Group 2 = chapter: digits (may be glued directly to letters, e.g. "Genesis1")
        // Group 3 = verse  : optional, after ':', ' ', or '.'
        // Group 4 = end    : optional verse-range end after '-'
        val pattern = Regex(
            """^(\d?\s*[A-Za-z]+(?:\s+[A-Za-z]+)?)\s*(\d+)(?:[\s:.]\s*(\d+)(?:\s*[-–]\s*(\d+))?)?$"""
        )
        val match = pattern.matchEntire(s) ?: return null

        val bookRaw = match.groupValues[1].trim()
        val chapter = match.groupValues[2].toIntOrNull() ?: return null
        val verseStart = match.groupValues[3].toIntOrNull()
        val verseEnd = match.groupValues[4].toIntOrNull()

        val resolvedBook = fuzzyResolveBook(bookRaw) ?: bookRaw

        return ScriptureRef(
            book = resolvedBook,
            chapter = chapter,
            verseStart = verseStart,
            verseEnd = verseEnd ?: verseStart,
        )
    }

    /**
     * Resolve a potentially misspelled or abbreviated book name to its canonical form.
     * Strategy: exact alias match → prefix match → Levenshtein fuzzy match.
     */
    internal fun fuzzyResolveBook(input: String): String? {
        val q = input.trim().lowercase()

        // 1. Exact alias match
        BOOK_ALIASES.entries.firstOrNull { (_, aliases) ->
            aliases.any { it.equals(q, ignoreCase = true) }
        }?.key?.let { return it }

        // 2. Prefix match (require at least 3 chars)
        if (q.length >= 3) {
            BOOK_ALIASES.entries.firstOrNull { (_, aliases) ->
                aliases.any { alias ->
                    alias.length >= q.length && alias.startsWith(q, ignoreCase = true)
                }
            }?.key?.let { return it }
        }

        // 3. Levenshtein fuzzy match; threshold scales with query length
        val threshold = when {
            q.length <= 3 -> 1
            q.length <= 5 -> 2
            else -> 3
        }
        var bestBook: String? = null
        var bestDist = Int.MAX_VALUE

        for ((book, aliases) in BOOK_ALIASES) {
            for (alias in aliases) {
                if (kotlin.math.abs(alias.length - q.length) > threshold + 1) continue
                val dist = levenshtein(q, alias.lowercase())
                if (dist < bestDist && dist <= threshold) {
                    bestDist = dist
                    bestBook = book
                }
            }
        }
        return bestBook
    }

    private fun levenshtein(a: String, b: String): Int {
        val m = a.length; val n = b.length
        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in 0..m) dp[i][0] = i
        for (j in 0..n) dp[0][j] = j
        for (i in 1..m) for (j in 1..n) {
            dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
            else 1 + min(dp[i - 1][j - 1], min(dp[i - 1][j], dp[i][j - 1]))
        }
        return dp[m][n]
    }

    private data class ScriptureRef(
        val book: String,
        val chapter: Int,
        val verseStart: Int?,
        val verseEnd: Int?,
    )

    private data class MultiScriptureRef(
        val refs: List<ScriptureRef>
    )

    companion object {
        /** Maps canonical book name → recognised aliases (all lowercase). */
        val BOOK_ALIASES: Map<String, List<String>> = mapOf(
            "Genesis"          to listOf("genesis", "gen", "ge"),
            "Exodus"           to listOf("exodus", "exod", "exo", "ex"),
            "Leviticus"        to listOf("leviticus", "lev", "le"),
            "Numbers"          to listOf("numbers", "num", "nu", "nb"),
            "Deuteronomy"      to listOf("deuteronomy", "deut", "deu", "dt"),
            "Joshua"           to listOf("joshua", "josh", "jos"),
            "Judges"           to listOf("judges", "judg", "jdg"),
            "Ruth"             to listOf("ruth", "ru"),
            "1 Samuel"         to listOf("1 samuel", "1samuel", "1sam", "1sa"),
            "2 Samuel"         to listOf("2 samuel", "2samuel", "2sam", "2sa"),
            "1 Kings"          to listOf("1 kings", "1kings", "1ki", "1kgs"),
            "2 Kings"          to listOf("2 kings", "2kings", "2ki", "2kgs"),
            "1 Chronicles"     to listOf("1 chronicles", "1chronicles", "1chr", "1ch", "1chron"),
            "2 Chronicles"     to listOf("2 chronicles", "2chronicles", "2chr", "2ch", "2chron"),
            "Ezra"             to listOf("ezra", "ez"),
            "Nehemiah"         to listOf("nehemiah", "neh", "ne"),
            "Esther"           to listOf("esther", "est", "esth"),
            "Job"              to listOf("job"),
            "Psalms"           to listOf("psalms", "psalm", "ps", "psa"),
            "Proverbs"         to listOf("proverbs", "prov", "pro", "prv"),
            "Ecclesiastes"     to listOf("ecclesiastes", "eccl", "ecc", "qoh"),
            "Song of Solomon"  to listOf("song of solomon", "song of songs", "song", "sos", "cant", "sg"),
            "Isaiah"           to listOf("isaiah", "isa", "is"),
            "Jeremiah"         to listOf("jeremiah", "jer", "je"),
            "Lamentations"     to listOf("lamentations", "lam", "la"),
            "Ezekiel"          to listOf("ezekiel", "ezek", "eze"),
            "Daniel"           to listOf("daniel", "dan", "da"),
            "Hosea"            to listOf("hosea", "hos", "ho"),
            "Joel"             to listOf("joel", "jl"),
            "Amos"             to listOf("amos", "am"),
            "Obadiah"          to listOf("obadiah", "obad", "ob"),
            "Jonah"            to listOf("jonah", "jon"),
            "Micah"            to listOf("micah", "mic"),
            "Nahum"            to listOf("nahum", "nah", "na"),
            "Habakkuk"         to listOf("habakkuk", "hab"),
            "Zephaniah"        to listOf("zephaniah", "zeph", "zep"),
            "Haggai"           to listOf("haggai", "hag"),
            "Zechariah"        to listOf("zechariah", "zech", "zec"),
            "Malachi"          to listOf("malachi", "mal"),
            "Matthew"          to listOf("matthew", "matt", "mat", "mt"),
            "Mark"             to listOf("mark", "mk", "mar"),
            "Luke"             to listOf("luke", "lk", "luk"),
            "John"             to listOf("john", "jn", "joh"),
            "Acts"             to listOf("acts", "act", "ac"),
            "Romans"           to listOf("romans", "rom", "ro"),
            "1 Corinthians"    to listOf("1 corinthians", "1corinthians", "1cor", "1co"),
            "2 Corinthians"    to listOf("2 corinthians", "2corinthians", "2cor", "2co"),
            "Galatians"        to listOf("galatians", "gal", "ga"),
            "Ephesians"        to listOf("ephesians", "eph"),
            "Philippians"      to listOf("philippians", "phil"),
            "Colossians"       to listOf("colossians", "col"),
            "1 Thessalonians"  to listOf("1 thessalonians", "1thessalonians", "1thess", "1th"),
            "2 Thessalonians"  to listOf("2 thessalonians", "2thessalonians", "2thess", "2th"),
            "1 Timothy"        to listOf("1 timothy", "1timothy", "1tim", "1ti"),
            "2 Timothy"        to listOf("2 timothy", "2timothy", "2tim", "2ti"),
            "Titus"            to listOf("titus", "tit"),
            "Philemon"         to listOf("philemon", "phlm", "phm"),
            "Hebrews"          to listOf("hebrews", "heb"),
            "James"            to listOf("james", "jas", "jm"),
            "1 Peter"          to listOf("1 peter", "1peter", "1pet", "1pe"),
            "2 Peter"          to listOf("2 peter", "2peter", "2pet", "2pe"),
            "1 John"           to listOf("1 john", "1john", "1jn", "1jo"),
            "2 John"           to listOf("2 john", "2john", "2jn", "2jo"),
            "3 John"           to listOf("3 john", "3john", "3jn", "3jo"),
            "Jude"             to listOf("jude", "jud"),
            "Revelation"       to listOf("revelation", "rev", "re", "apoc", "apocalypse"),
        )
    }
}
