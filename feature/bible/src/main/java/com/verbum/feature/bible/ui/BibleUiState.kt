package com.verbum.feature.bible.ui

import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.BibleCrossReference
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.ChapterBlock
import com.verbum.feature.bible.ui.reading.ReadingMode
import com.verbum.feature.bible.ui.reading.theme.ReadingThemeType

sealed interface BibleUiState {
    data object Loading : BibleUiState
    data class BooksLoaded(
        val oldTestament: List<BibleBook>,
        val newTestament: List<BibleBook>,
        val searchQuery: String = "",
        val searchResults: List<Verse> = emptyList(),
        val isSearching: Boolean = false,
        val isRefreshingOnline: Boolean = false,
        val onlineMessage: String? = null,
    ) : BibleUiState
    data class Error(val message: String) : BibleUiState
}

sealed interface BibleReaderUiState {
    data object Loading : BibleReaderUiState
    data class Loaded(
        val bookName: String,
        val chapter: Int,
        val totalChapters: Int,
        val verses: List<Verse>,
        val selectedVerse: Verse? = null,
        val readingMode: ReadingMode = ReadingMode.SCROLL,
        val themeType: ReadingThemeType = ReadingThemeType.CLASSIC,
        val chapterBlocks: List<ChapterBlock> = emptyList(),
        val showChapterNav: Boolean = false,
        val showSearch: Boolean = false,
        val searchQuery: String = "",
        val searchSuggestions: List<Verse> = emptyList(),
        val searchResults: List<Verse> = emptyList(),
        val isLoadingNextChapter: Boolean = false,
        /** When non-null, ScrollReadingView will scroll to this verse then clear it. */
        val targetVerse: Int? = null,
        val crossReferences: List<BibleCrossReference> = emptyList(),
        val isLoadingCrossReferences: Boolean = false,
    ) : BibleReaderUiState
    data class Error(val message: String) : BibleReaderUiState
}
