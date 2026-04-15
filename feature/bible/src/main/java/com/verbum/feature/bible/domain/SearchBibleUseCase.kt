package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.Verse
import javax.inject.Inject

class SearchBibleUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(query: String): List<Verse> {
        if (query.length < 3) return emptyList()
        return repository.searchVerses(query)
    }
}
