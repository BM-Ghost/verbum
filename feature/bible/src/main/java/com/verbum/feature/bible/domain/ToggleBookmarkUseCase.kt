package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(bookId: Int, chapter: Int, verse: Int) {
        repository.toggleBookmark(bookId, chapter, verse)
    }
}
