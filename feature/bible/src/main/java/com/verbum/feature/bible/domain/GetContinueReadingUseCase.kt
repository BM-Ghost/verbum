package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.data.ReadingPosition
import com.verbum.feature.bible.domain.model.BibleBook
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class ContinueReadingState(
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val lastVerse: Int,
)

/**
 * Use case for getting the continue reading state (last position + book name).
 * Combines reading position from the repository with book metadata.
 */
class GetContinueReadingUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    operator fun invoke(): Flow<ContinueReadingState?> {
        return repository.getLastReadPosition()
            .flatMapLatest { position ->
                if (position == null) return@flatMapLatest flowOf(null)
                repository.getAllBooks().map { books ->
                    val bookName = books.firstOrNull { it.id == position.bookId }?.name ?: "Unknown"
                    ContinueReadingState(
                        bookId = position.bookId,
                        bookName = bookName,
                        chapter = position.chapter,
                        lastVerse = position.lastVerse,
                    )
                }
            }
    }
}
