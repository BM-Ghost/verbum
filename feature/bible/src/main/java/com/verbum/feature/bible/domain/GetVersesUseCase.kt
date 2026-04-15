package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.Verse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVersesUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    operator fun invoke(bookId: Int, chapter: Int): Flow<List<Verse>> =
        repository.getVerses(bookId, chapter)
}
