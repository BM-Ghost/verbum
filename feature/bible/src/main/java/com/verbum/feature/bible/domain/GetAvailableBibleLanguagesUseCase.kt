package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import com.verbum.feature.bible.domain.model.BibleLanguage
import javax.inject.Inject

class GetAvailableBibleLanguagesUseCase @Inject constructor(
    private val repository: BibleRepository,
) {
    suspend operator fun invoke(): List<BibleLanguage> = repository.getAvailableLanguages()
}
