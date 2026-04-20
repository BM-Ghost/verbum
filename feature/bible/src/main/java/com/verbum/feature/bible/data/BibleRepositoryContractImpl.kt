package com.verbum.feature.bible.data

import androidx.paging.PagingData
import com.verbum.feature.bible.domain.contract.BibleRepositoryContract
import com.verbum.feature.bible.domain.model.Verse
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class BibleRepositoryContractImpl @Inject constructor(
    private val bibleRepository: BibleRepository,
) : BibleRepositoryContract {
    override fun getVerses(book: String, chapter: Int): Flow<PagingData<Verse>> {
        return bibleRepository.getVerses(book, chapter)
    }
}
