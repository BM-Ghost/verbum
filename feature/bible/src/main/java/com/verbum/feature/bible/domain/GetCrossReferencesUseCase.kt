package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.BibleCrossReference
import javax.inject.Inject

class GetCrossReferencesUseCase @Inject constructor(
    private val bibleRepository: BibleRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        chapter: Int,
        verse: Int,
        limit: Int = 20,
    ): List<BibleCrossReference> {
        return bibleRepository.getCrossReferences(bookId, chapter, verse, limit.coerceIn(1, 50))
    }
}