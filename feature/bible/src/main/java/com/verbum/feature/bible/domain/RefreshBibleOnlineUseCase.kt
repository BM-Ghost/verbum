package com.verbum.feature.bible.domain

import com.verbum.feature.bible.data.BibleRepository
import javax.inject.Inject

class RefreshBibleOnlineUseCase @Inject constructor(
    private val bibleRepository: BibleRepository,
) {
    suspend operator fun invoke(): Result<Int> = bibleRepository.refreshDrcFromOnline()
}