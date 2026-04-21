package com.verbum.feature.bible.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.GetChapterCountUseCase
import com.verbum.feature.bible.domain.GetVersesUseCase
import com.verbum.feature.bible.domain.SearchBibleUseCase
import com.verbum.feature.bible.domain.ToggleBookmarkUseCase
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.ChapterBlock
import com.verbum.feature.bible.ui.reading.ReadingMode
import com.verbum.feature.bible.ui.reading.theme.ReadingThemeType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BibleReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVerses: GetVersesUseCase,
    private val getChapterCount: GetChapterCountUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val searchBible: SearchBibleUseCase,
    private val repository: BibleRepository,
) : ViewModel() {

    private val bookId: Int = savedStateHandle.get<Int>("bookId") ?: 1
    private val initialChapter: Int = savedStateHandle.get<Int>("chapter") ?: 1
    private val selectedChapter = MutableStateFlow(initialChapter)
    private val totalChapters = MutableStateFlow(1)
    private var pendingNavigationChapter: Int? = null
    private var searchJob: Job? = null
    private var suggestionJob: Job? = null

    private val _uiState = MutableStateFlow<BibleReaderUiState>(BibleReaderUiState.Loading)
    val uiState: StateFlow<BibleReaderUiState> = _uiState.asStateFlow()

    init {
        loadSavedPreferences()
        loadChapterCount()
        observeVerses()
    }

    private fun recordProgress(chapter: Int, verse: Int = 1) {
        viewModelScope.launch {
            repository.recordReadingPosition(bookId, chapter, verse)
        }
    }

    private fun loadSavedPreferences() {
        viewModelScope.launch {
            val savedTheme = repository.getReadingTheme()?.let { id ->
                ReadingThemeType.entries.firstOrNull { it.name == id }
            }
            val savedMode = repository.getReadingMode()?.let { name ->
                ReadingMode.entries.firstOrNull { it.name == name }
            }

            _uiState.first { it is BibleReaderUiState.Loaded }.let { state ->
                val loaded = state as BibleReaderUiState.Loaded
                val theme = savedTheme ?: loaded.themeType
                val resolvedTheme = theme.resolve(false)
                val mode = when {
                    savedMode != null && resolvedTheme.allowedModes?.contains(savedMode.name) != false -> savedMode
                    resolvedTheme.allowedModes != null ->
                        ReadingMode.entries.firstOrNull { it.name in (resolvedTheme.allowedModes ?: emptyList()) } ?: ReadingMode.SCROLL
                    else -> savedMode ?: loaded.readingMode
                }
                _uiState.value = loaded.copy(themeType = theme, readingMode = mode)
            }
        }
    }

    private fun loadChapterCount() {
        viewModelScope.launch {
            val chapterCount = getChapterCount(bookId)
            totalChapters.value = chapterCount
            val current = _uiState.value
            if (current is BibleReaderUiState.Loaded) {
                _uiState.value = current.copy(totalChapters = chapterCount)
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeVerses() {
        selectedChapter
            .flatMapLatest { chapter ->
                getVerses(bookId, chapter)
                    .asResult()
                    .map { result -> chapter to result }
            }
            .onEach { (chapter, result) ->
                val existing = _uiState.value as? BibleReaderUiState.Loaded
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> existing?.copy(chapter = chapter) ?: BibleReaderUiState.Loading
                    is VerbumResult.Success -> {
                        val verses = result.data
                        val existingState = (_uiState.value as? BibleReaderUiState.Loaded) ?: existing

                        val existingBlocks = existingState?.chapterBlocks.orEmpty()
                        val newBlock = ChapterBlock(chapter = chapter, verses = verses)
                        val updatedBlocks = if (existingBlocks.any { it.chapter == chapter }) {
                            existingBlocks.map { if (it.chapter == chapter) newBlock else it }
                        } else {
                            (existingBlocks + newBlock).sortedBy { it.chapter }
                        }

                        BibleReaderUiState.Loaded(
                            bookName = verses.firstOrNull()?.bookName.orEmpty(),
                            chapter = chapter,
                            totalChapters = totalChapters.value,
                            verses = verses,
                            readingMode = existingState?.readingMode ?: ReadingMode.SCROLL,
                            themeType = existingState?.themeType ?: ReadingThemeType.CLASSIC,
                            chapterBlocks = updatedBlocks,
                            showChapterNav = existingState?.showChapterNav ?: false,
                            showSearch = existingState?.showSearch ?: false,
                            searchQuery = existingState?.searchQuery.orEmpty(),
                            searchSuggestions = existingState?.searchSuggestions.orEmpty(),
                            searchResults = existingState?.searchResults.orEmpty(),
                            isLoadingNextChapter = existingState?.isLoadingNextChapter ?: false,
                            targetVerse = existingState?.targetVerse,
                        )
                    }
                    is VerbumResult.Error -> BibleReaderUiState.Error("Failed to load chapter")
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPreviousChapter() {
        val current = _uiState.value
        if (current is BibleReaderUiState.Loaded && current.chapter > 1) {
            navigateToChapter(current.chapter - 1)
        }
    }

    fun onNextChapter() {
        val current = _uiState.value
        if (current is BibleReaderUiState.Loaded && current.chapter < current.totalChapters) {
            navigateToChapter(current.chapter + 1)
        }
    }

    fun onVisibleChapterChange(chapter: Int) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return

        val pending = pendingNavigationChapter
        if (pending != null) {
            if (chapter == pending) {
                pendingNavigationChapter = null
            } else {
                return
            }
        }

        if (current.chapter != chapter) {
            _uiState.value = current.copy(chapter = chapter)
            recordProgress(chapter)
        }
    }

    fun onVisibleVerseChange(chapter: Int, firstVerse: Int) {
        recordProgress(chapter, firstVerse)
    }

    fun onChapterSelected(chapter: Int) {
        val current = _uiState.value
        if (current is BibleReaderUiState.Loaded) {
            _uiState.value = current.copy(showChapterNav = false)
            navigateToChapter(chapter)
        }
    }

    private fun navigateToChapter(chapter: Int) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        if (chapter !in 1..current.totalChapters) return

        pendingNavigationChapter = if (current.readingMode == ReadingMode.SCROLL) chapter else null

        val loadedBlock = current.chapterBlocks.firstOrNull { it.chapter == chapter }
        _uiState.value = if (loadedBlock != null) {
            current.copy(chapter = chapter, verses = loadedBlock.verses)
        } else {
            current.copy(chapter = chapter)
        }

        if (selectedChapter.value != chapter) {
            selectedChapter.value = chapter
        }

        recordProgress(chapter)
    }

    fun onLoadNextChapter() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        if (current.isLoadingNextChapter) return

        val lastLoaded = current.chapterBlocks.maxOfOrNull { it.chapter } ?: current.chapter
        val nextChapter = lastLoaded + 1
        if (nextChapter > current.totalChapters) return

        _uiState.value = current.copy(isLoadingNextChapter = true)

        viewModelScope.launch {
            try {
                val verses = getVerses(bookId, nextChapter).first()
                val updated = _uiState.value as? BibleReaderUiState.Loaded ?: return@launch
                val newBlock = ChapterBlock(chapter = nextChapter, verses = verses)
                _uiState.value = updated.copy(
                    chapterBlocks = (updated.chapterBlocks + newBlock).sortedBy { it.chapter },
                    isLoadingNextChapter = false,
                )
            } catch (_: Exception) {
                val updated = _uiState.value as? BibleReaderUiState.Loaded ?: return@launch
                _uiState.value = updated.copy(isLoadingNextChapter = false)
            }
        }
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

    fun onReadingModeChange(mode: ReadingMode) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        val theme = current.themeType.resolve(false)
        val allowed = theme.allowedModes
        if (allowed != null && mode.name !in allowed) return

        _uiState.value = current.copy(readingMode = mode)
        viewModelScope.launch { repository.setReadingMode(mode.name) }
    }

    fun onThemeChange(themeType: ReadingThemeType) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        val newTheme = themeType.resolve(false)
        val newMode = newTheme.allowedModes?.let { modes ->
            if (current.readingMode.name !in modes) {
                ReadingMode.entries.firstOrNull { it.name in modes } ?: ReadingMode.SCROLL
            } else {
                current.readingMode
            }
        } ?: current.readingMode
        _uiState.value = current.copy(themeType = themeType, readingMode = newMode)
        viewModelScope.launch {
            repository.setReadingTheme(themeType.name)
            repository.setReadingMode(newMode.name)
        }
    }

    fun onToggleChapterNav() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(showChapterNav = !current.showChapterNav)
    }

    fun onToggleSearch() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(
            showSearch = !current.showSearch,
            searchQuery = if (current.showSearch) "" else current.searchQuery,
            searchSuggestions = if (current.showSearch) emptyList() else current.searchSuggestions,
            searchResults = if (current.showSearch) emptyList() else current.searchResults,
        )
    }

    fun onSearchQueryChange(query: String) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        suggestionJob?.cancel()
        searchJob?.cancel()
        _uiState.value = current.copy(
            searchQuery = query,
            searchSuggestions = emptyList(),
            searchResults = emptyList(),
        )

        // While typing, show lightweight suggestions only.
        if (query.length >= 2 && parseInReaderShorthand(query.trim(), current.chapter) == null) {
            suggestionJob = viewModelScope.launch {
                delay(300)
                val results = searchBible(query)
                val updated = _uiState.value as? BibleReaderUiState.Loaded ?: return@launch
                if (updated.searchQuery == query) {
                    _uiState.value = updated.copy(searchSuggestions = results.take(8))
                }
            }
        }
    }

    fun onSearch() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        val q = current.searchQuery.trim()

        val nav = parseInReaderShorthand(q, current.chapter)
        if (nav != null) {
            suggestionJob?.cancel()
            searchJob?.cancel()
            _uiState.value = current.copy(
                showSearch = false,
                searchQuery = "",
                searchSuggestions = emptyList(),
                searchResults = emptyList(),
                targetVerse = nav.verse,
            )
            if (nav.chapter != current.chapter) {
                navigateToChapter(nav.chapter)
            }
            return
        }

        if (q.length < 2) return

        suggestionJob?.cancel()
        searchJob?.cancel()
        viewModelScope.launch {
            val results = searchBible(q)
            val updated = _uiState.value as? BibleReaderUiState.Loaded ?: return@launch
            _uiState.value = updated.copy(
                searchSuggestions = emptyList(),
                searchResults = results,
            )
        }
    }

    fun onClearSearch() {
        suggestionJob?.cancel()
        searchJob?.cancel()
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(
            searchQuery = "",
            searchSuggestions = emptyList(),
            searchResults = emptyList(),
        )
    }

    fun onSearchSuggestionClick(verse: Verse) {
        suggestionJob?.cancel()
        searchJob?.cancel()
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(
            showSearch = false,
            searchQuery = "",
            searchSuggestions = emptyList(),
            searchResults = emptyList(),
            targetVerse = verse.verseNumber,
        )
        selectedChapter.value = verse.chapter
    }

    fun onSearchResultClick(verse: Verse) {
        suggestionJob?.cancel()
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(
            showSearch = false,
            searchResults = emptyList(),
            searchQuery = "",
            searchSuggestions = emptyList(),
            targetVerse = verse.verseNumber,
        )
        selectedChapter.value = verse.chapter
    }

    fun onTargetVerseConsumed() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(targetVerse = null)
    }

    private fun parseInReaderShorthand(query: String, currentChapter: Int): InReaderNav? {
        val s = query.trim()

        if (s.matches(Regex("\\d+"))) {
            val verse = s.toIntOrNull() ?: return null
            return InReaderNav(currentChapter, verse)
        }

        val m = Regex("""^(\d+)\s*[:\s.]\s*(\d+)$""").matchEntire(s) ?: return null
        val ch = m.groupValues[1].toIntOrNull() ?: return null
        val v = m.groupValues[2].toIntOrNull()
        return InReaderNav(ch, v)
    }

    private data class InReaderNav(val chapter: Int, val verse: Int?)
}
