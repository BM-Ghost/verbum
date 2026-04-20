package com.verbum.feature.bible.domain.contract

import androidx.paging.PagingData
import com.verbum.feature.bible.domain.model.Verse
import kotlinx.coroutines.flow.Flow

interface BibleRepositoryContract {
    fun getVerses(book: String, chapter: Int): Flow<PagingData<Verse>>
}
