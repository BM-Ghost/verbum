package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import javax.inject.Inject

class SetBibleLanguageUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(languageCode: String) = repository.setSelectedLanguageCode(languageCode)
}
