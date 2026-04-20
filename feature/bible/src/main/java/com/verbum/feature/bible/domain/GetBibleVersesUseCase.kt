package com.verbum.feature.bible.domain

import androidx.paging.PagingData
import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.Verse
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetBibleVersesUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    operator fun invoke(bookId: Int, chapter: Int): Flow<PagingData<Verse>> {
        return repository.getPagedVerses(bookId, chapter)
    }
}
