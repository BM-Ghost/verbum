package com.verbum.feature.bible.domain

import androidx.paging.PagingData
import com.verbum.feature.bible.domain.contract.BibleRepositoryContract
import com.verbum.feature.bible.domain.model.Verse
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetBibleVerses @Inject constructor(
    private val repository: BibleRepositoryContract,
) {
    operator fun invoke(book: String, chapter: Int): Flow<PagingData<Verse>> {
        return repository.getVerses(book, chapter)
    }
}
