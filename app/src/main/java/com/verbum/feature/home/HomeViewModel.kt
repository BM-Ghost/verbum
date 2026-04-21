package com.verbum.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.feature.bible.domain.ContinueReadingState
import com.verbum.feature.bible.domain.GetContinueReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getContinueReading: GetContinueReadingUseCase,
) : ViewModel() {

    val continueReading = getContinueReading()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
