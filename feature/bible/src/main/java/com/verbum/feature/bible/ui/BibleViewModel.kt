package com.verbum.feature.bible.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.bible.domain.GetBibleBooksUseCase
import com.verbum.feature.bible.domain.SearchBibleUseCase
import com.verbum.feature.bible.domain.model.Testament
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibleViewModel @Inject constructor(
    private val getBibleBooks: GetBibleBooksUseCase,
    private val searchBible: SearchBibleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BibleUiState>(BibleUiState.Loading)
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null

    init {
        observeBooks()
    }

    private fun observeBooks() {
        getBibleBooks()
            .asResult()
            .onEach { result ->
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> BibleUiState.Loading
                    is VerbumResult.Success -> {
                        val books = result.data
                        BibleUiState.BooksLoaded(
                            oldTestament = books.filter { it.testament == Testament.OLD },
                            newTestament = books.filter { it.testament == Testament.NEW },
                        )
                    }
                    is VerbumResult.Error -> BibleUiState.Error(
                        result.message ?: "Failed to load Bible books"
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        val current = _uiState.value
        if (current !is BibleUiState.BooksLoaded) return

        _uiState.value = current.copy(searchQuery = query, isSearching = query.length >= 2)

        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                val results = searchBible(query)
                val latest = _uiState.value
                if (latest is BibleUiState.BooksLoaded) {
                    _uiState.value = latest.copy(searchResults = results, isSearching = false)
                }
            }
        } else {
            _uiState.value = current.copy(searchQuery = query, searchResults = emptyList(), isSearching = false)
        }
    }
}
