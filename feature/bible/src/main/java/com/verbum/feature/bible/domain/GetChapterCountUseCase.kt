package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import javax.inject.Inject

class GetChapterCountUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(bookId: Int): Int = repository.getChapterCount(bookId)
}
