package com.verbum.feature.bible.ui

import com.verbum.feature.bible.domain.model.BibleBook
import com.verbum.feature.bible.domain.model.Verse

sealed interface BibleUiState {
    data object Loading : BibleUiState
    data class BooksLoaded(
        val oldTestament: List<BibleBook>,
        val newTestament: List<BibleBook>,
        val searchQuery: String = "",
        val searchResults: List<Verse> = emptyList(),
        val isSearching: Boolean = false,
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
    ) : BibleReaderUiState
    data class Error(val message: String) : BibleReaderUiState
}
