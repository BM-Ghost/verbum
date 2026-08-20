package com.verbum.feature.bible.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.GetChapterCountUseCase
import com.verbum.feature.bible.domain.GetCrossReferencesUseCase
import com.verbum.feature.bible.domain.GetVersesUseCase
import com.verbum.feature.bible.domain.SearchBibleUseCase
import com.verbum.feature.bible.domain.ToggleBookmarkUseCase
import com.verbum.feature.bible.domain.model.Verse
import com.verbum.feature.bible.ui.reading.ChapterBlock
import com.verbum.feature.bible.ui.reading.ReadingMode
import com.verbum.feature.bible.ui.reading.theme.ReadingThemeType
import com.verbum.feature.bible.ui.TargetVerseLocation
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class BibleReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVerses: GetVersesUseCase,
    private val getChapterCount: GetChapterCountUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val searchBible: SearchBibleUseCase,
    private val repository: BibleRepository,
    private val getCrossReferences: GetCrossReferencesUseCase,
) : ViewModel() {

    private val bookId: Int = savedStateHandle.get<Int>("bookId") ?: 1
    private val initialChapter: Int = savedStateHandle.get<Int>("chapter") ?: 1
    private var pendingInitialVerse: Int? = savedStateHandle.get<Int>("verse")?.takeIf { it > 0 }
    private val selectedChapter = MutableStateFlow(initialChapter)
    private val totalChapters = MutableStateFlow(1)
    private var pendingNavigationChapter: Int? = null
    private var searchJob: Job? = null
    private var suggestionJob: Job? = null
    private var onBookSwitchNeeded: ((Int, Int, Int) -> Unit)? = null
    private var pendingTargetVerses: List<Verse>? = null

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

                        val loadedState = BibleReaderUiState.Loaded(
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
                            targetVerses = existingState?.targetVerses ?: if (pendingInitialVerse != null) setOf(pendingInitialVerse!!) else emptySet(),
                            targetVerseRange = existingState?.targetVerseRange,
                            currentTargetIndex = existingState?.currentTargetIndex ?: 0,
                            targetVerseLocations = existingState?.targetVerseLocations ?: if (pendingInitialVerse != null) {
                                listOf(TargetVerseLocation(bookId, verses.firstOrNull()?.bookName.orEmpty(), chapter, pendingInitialVerse!!))
                            } else emptyList(),
                            crossReferences = existingState?.crossReferences.orEmpty(),
                            isLoadingCrossReferences = existingState?.isLoadingCrossReferences ?: false,
                            currentVerse = existingState?.currentVerse ?: 1,
                        )
                        
                        loadedState.also { pendingInitialVerse = null }
                    }
                    is VerbumResult.Error -> BibleReaderUiState.Error("Failed to load chapter")
                }
                
                // Apply pending target verses if state is now Loaded
                val currentState = _uiState.value
                if (currentState is BibleReaderUiState.Loaded && pendingTargetVerses != null) {
                    Timber.d("observeVerses: Applying pending target verses (${pendingTargetVerses!!.size})")
                    val tempVerses = pendingTargetVerses
                    pendingTargetVerses = null
                    setTargetVerses(tempVerses!!)
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
            _uiState.value = current.copy(
                selectedVerse = verse,
                crossReferences = emptyList(),
                isLoadingCrossReferences = true,
            )
            viewModelScope.launch {
                val references = runCatching {
                    getCrossReferences(bookId, verse.chapter, verse.verseNumber)
                }.getOrDefault(emptyList())
                val loaded = _uiState.value as? BibleReaderUiState.Loaded ?: return@launch
                if (loaded.selectedVerse == verse) {
                    _uiState.value = loaded.copy(
                        crossReferences = references,
                        isLoadingCrossReferences = false,
                    )
                }
            }
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
            val targetVerses = if (nav.verse != null) setOf(nav.verse) else emptySet()
            val targetLocations = if (nav.verse != null) {
                listOf(TargetVerseLocation(bookId, current.bookName, nav.chapter, nav.verse))
            } else emptyList()
            _uiState.value = current.copy(
                showSearch = false,
                searchQuery = "",
                searchSuggestions = emptyList(),
                searchResults = emptyList(),
                targetVerses = targetVerses,
                targetVerseLocations = targetLocations,
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
            targetVerses = setOf(verse.verseNumber),
            targetVerseLocations = listOf(TargetVerseLocation(verse.bookId, verse.bookName, verse.chapter, verse.verseNumber)),
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
            targetVerses = setOf(verse.verseNumber),
            targetVerseLocations = listOf(TargetVerseLocation(verse.bookId, verse.bookName, verse.chapter, verse.verseNumber)),
        )
        selectedChapter.value = verse.chapter
    }

    fun onTargetVerseConsumed() {
        // No-op - highlights persist until book exit
    }

    fun hasNextTargetVerse(): Boolean {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return false
        val result = current.currentTargetIndex < current.targetVerseLocations.size - 1
        Timber.d("hasNextTargetVerse: currentTargetIndex=${current.currentTargetIndex}, totalLocations=${current.targetVerseLocations.size}, result=$result")
        return result
    }

    fun hasPreviousTargetVerse(): Boolean {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return false
        val result = current.currentTargetIndex > 0
        Timber.d("hasPreviousTargetVerse: currentTargetIndex=${current.currentTargetIndex}, result=$result")
        return result
    }

    fun goToNextTargetVerse() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        Timber.d("goToNextTargetVerse called")
        if (!hasNextTargetVerse()) {
            Timber.d("No next target verse available")
            return
        }
        
        // Find the next location with a different book or chapter
        val nextLocation = current.targetVerseLocations
            .drop(current.currentTargetIndex + 1)
            .firstOrNull { it.bookId != bookId || it.chapter != current.chapter }
        
        if (nextLocation != null) {
            // Navigate to the next chapter/book
            val newIndex = current.targetVerseLocations.indexOf(nextLocation)
            Timber.d("Navigating to next chapter/book: index=$newIndex, location=${nextLocation.bookName} ${nextLocation.chapter}:${nextLocation.verse}")
            navigateToLocation(nextLocation, newIndex)
        } else {
            Timber.d("No more chapters/books to navigate to")
        }
    }

    fun goToPreviousTargetVerse() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        Timber.d("goToPreviousTargetVerse called")
        if (!hasPreviousTargetVerse()) {
            Timber.d("No previous target verse available")
            return
        }
        
        // Find the previous location with a different book or chapter
        val previousLocation = current.targetVerseLocations
            .take(current.currentTargetIndex)
            .lastOrNull { it.bookId != bookId || it.chapter != current.chapter }
        
        if (previousLocation != null) {
            // Navigate to the previous chapter/book
            val newIndex = current.targetVerseLocations.indexOf(previousLocation)
            Timber.d("Navigating to previous chapter/book: index=$newIndex, location=${previousLocation.bookName} ${previousLocation.chapter}:${previousLocation.verse}")
            navigateToLocation(previousLocation, newIndex)
        } else {
            Timber.d("No more chapters/books to navigate to")
        }
    }

    fun setTargetVerses(verses: List<Verse>) {
        val current = _uiState.value as? BibleReaderUiState.Loaded
        if (current == null) {
            Timber.d("setTargetVerses: UI state not Loaded, storing pending verses (${verses.size})")
            pendingTargetVerses = verses
            return
        }
        
        Timber.d("setTargetVerses called with ${verses.size} verses")
        Timber.d("Verses: ${verses.map { "${it.bookName} ${it.chapter}:${it.verseNumber}" }}")
        Timber.d("Current bookId: $bookId, current chapter: ${current.chapter}")
        
        val targetLocations = verses.map { 
            TargetVerseLocation(it.bookId, it.bookName, it.chapter, it.verseNumber) 
        }
        
        // Group verses by chapter AND book
        val versesByBookAndChapter = verses.groupBy { "${it.bookId}_${it.chapter}" }
        
        // Set target verses for current chapter (only if same book)
        val currentKey = "${bookId}_${current.chapter}"
        val currentChapterVerses = versesByBookAndChapter[currentKey]?.map { it.verseNumber }?.toSet() ?: emptySet()
        
        Timber.d("Current chapter verses: $currentChapterVerses")
        
        // Check if verses form a continuous range in current chapter
        val targetRange = if (currentChapterVerses.size > 1) {
            val sortedVerses = currentChapterVerses.sorted()
            val first = sortedVerses.first()
            val last = sortedVerses.last()
            val expectedRange = (first..last).toSet()
            if (expectedRange == currentChapterVerses) first to last else null
        } else null
        
        Timber.d("Target range: $targetRange")
        Timber.d("Target locations count: ${targetLocations.size}")
        
        _uiState.value = current.copy(
            targetVerses = currentChapterVerses,
            targetVerseRange = targetRange,
            targetVerseLocations = targetLocations,
            currentTargetIndex = 0,
        )
    }

    fun clearTargetVerses() {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        _uiState.value = current.copy(
            targetVerses = emptySet(),
            targetVerseRange = null,
            targetVerseLocations = emptyList(),
            currentTargetIndex = 0,
        )
    }

    private fun navigateToLocation(location: TargetVerseLocation, newIndex: Int = 0) {
        val current = _uiState.value as? BibleReaderUiState.Loaded ?: return
        
        Timber.d("navigateToLocation called: bookId=${location.bookId}, chapter=${location.chapter}, verse=${location.verse}, newIndex=$newIndex")
        Timber.d("Current bookId: $bookId, current chapter: ${current.chapter}")
        
        // Check if we need to switch books
        if (location.bookId != bookId) {
            Timber.d("Book switch needed: from $bookId to ${location.bookId}")
            Timber.d("onBookSwitchNeeded callback is ${if (onBookSwitchNeeded != null) "set" else "NULL"}")
            onBookSwitchNeeded?.invoke(location.bookId, location.chapter, location.verse)
            return
        }
        
        // Same book, navigate to chapter
        if (location.chapter != current.chapter) {
            Timber.d("Chapter navigation needed: from ${current.chapter} to ${location.chapter}")
            navigateToChapter(location.chapter)
        }
        
        // Update target verses for new chapter
        val versesInChapter = current.targetVerseLocations.filter { 
            it.chapter == location.chapter && it.bookId == bookId 
        }
        Timber.d("Verses in target chapter: ${versesInChapter.size}")
        if (versesInChapter.isNotEmpty()) {
            val verseNumbers = versesInChapter.map { it.verse }.toSet()
            
            // Check if verses form a continuous range
            val targetRange = if (verseNumbers.size > 1) {
                val sortedVerses = verseNumbers.sorted()
                val first = sortedVerses.first()
                val last = sortedVerses.last()
                val expectedRange = (first..last).toSet()
                if (expectedRange == verseNumbers) first to last else null
            } else null
            
            Timber.d("Updated target verses: $verseNumbers, range: $targetRange")
            
            _uiState.value = current.copy(
                targetVerses = verseNumbers,
                targetVerseRange = targetRange,
                currentTargetIndex = newIndex,
            )
        }
    }

    fun setBookSwitchCallback(callback: (Int, Int, Int) -> Unit) {
        onBookSwitchNeeded = callback
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
