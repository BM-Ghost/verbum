package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.BibleBook
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBibleBooksUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    operator fun invoke(): Flow<List<BibleBook>> = repository.getAllBooks()
}
