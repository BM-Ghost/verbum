package com.verbum.feature.bible.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.bible.domain.GetVersesUseCase
import com.verbum.feature.bible.domain.ToggleBookmarkUseCase
import com.verbum.feature.bible.domain.model.Verse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibleReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVerses: GetVersesUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val bookId: Int = savedStateHandle.get<Int>("bookId") ?: 1
    private val chapter: Int = savedStateHandle.get<Int>("chapter") ?: 1

    private val _uiState = MutableStateFlow<BibleReaderUiState>(BibleReaderUiState.Loading)
    val uiState: StateFlow<BibleReaderUiState> = _uiState.asStateFlow()

    init {
        loadVerses()
    }

    private fun loadVerses() {
        getVerses(bookId, chapter)
            .asResult()
            .onEach { result ->
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> BibleReaderUiState.Loading
                    is VerbumResult.Success -> {
                        val verses = result.data
                        BibleReaderUiState.Loaded(
                            bookName = verses.firstOrNull()?.bookName.orEmpty(),
                            chapter = chapter,
                            totalChapters = 50, // resolved dynamically in production
                            verses = verses,
                        )
                    }
                    is VerbumResult.Error -> BibleReaderUiState.Error("Failed to load chapter")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onVerseSelected(verse: Verse) {
        val current = _uiState.value
        if (current is BibleReaderUiState.Loaded) {
            _uiState.value = current.copy(selectedVerse = verse)
        }
    }

    fun onDismissVerseActions() {
        val current = _uiState.value
        if (current is BibleReaderUiState.Loaded) {
            _uiState.value = current.copy(selectedVerse = null)
        }
    }

    fun onToggleBookmark(verse: Verse) {
        viewModelScope.launch {
            toggleBookmark(verse.bookId, verse.chapter, verse.verseNumber)
        }
    }
}
