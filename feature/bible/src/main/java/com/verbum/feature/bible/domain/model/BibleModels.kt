package com.verbum.feature.bible.domain.model

data class BibleBook(
    val id: Int,
    val name: String,
    val abbreviation: String,
    val testament: Testament,
    val totalChapters: Int,
)

data class BibleLanguage(
    val code: String,
    val displayName: String,
)

enum class Testament(val displayName: String) {
    OLD("Old Testament"),
    NEW("New Testament"),
}

data class Verse(
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verseNumber: Int,
    val text: String,
    val isBookmarked: Boolean = false,
    val highlightColor: String? = null,
    val note: String? = null,
)

data class BibleCrossReference(
    val fromBookId: Int,
    val fromChapter: Int,
    val fromVerse: Int,
    val toBookId: Int,
    val toBookName: String,
    val toChapter: Int,
    val toVerseStart: Int,
    val toVerseEnd: Int,
    val votes: Int,
)

data class VerseAction(
    val verse: Verse,
    val type: VerseActionType,
)

enum class VerseActionType {
    Bookmark,
    Highlight,
    Note,
    Share,
    AskAi,
}
