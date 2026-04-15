package com.verbum.feature.missal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.core.common.extensions.asResult
import com.verbum.core.common.extensions.toLiturgicalKey
import com.verbum.core.common.result.VerbumResult
import com.verbum.feature.missal.domain.GetDailyReadingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MissalViewModel @Inject constructor(
    private val getDailyReadings: GetDailyReadingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MissalUiState>(MissalUiState.Loading)
    val uiState: StateFlow<MissalUiState> = _uiState.asStateFlow()

    private var currentDate: LocalDate = LocalDate.now()

    init {
        loadReadings(currentDate)
    }

    fun onDateSelected(date: LocalDate) {
        currentDate = date
        loadReadings(date)
    }

    private fun loadReadings(date: LocalDate) {
        getDailyReadings(date.toLiturgicalKey())
            .asResult()
            .onEach { result ->
                _uiState.value = when (result) {
                    is VerbumResult.Loading -> MissalUiState.Loading
                    is VerbumResult.Success -> MissalUiState.Loaded(
                        dailyReadings = result.data,
                        selectedDate = date.toLiturgicalKey(),
                    )
                    is VerbumResult.Error -> MissalUiState.Error("Could not load today's readings")
                }
            }
            .launchIn(viewModelScope)
    }
}
