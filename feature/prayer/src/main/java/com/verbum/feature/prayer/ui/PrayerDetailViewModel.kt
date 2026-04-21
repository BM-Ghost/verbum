package com.verbum.feature.prayer.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.verbum.feature.prayer.domain.GetPrayerByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class PrayerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPrayerById: GetPrayerByIdUseCase,
) : ViewModel() {

    private val prayerId: String = savedStateHandle.get<String>("prayerId").orEmpty().also {
        Timber.d("PrayerDetailViewModel initialized with prayerId='$it'")
    }

    private val _uiState = MutableStateFlow<PrayerDetailUiState>(PrayerDetailUiState.Loading)
    val uiState: StateFlow<PrayerDetailUiState> = _uiState.asStateFlow()

    init {
        loadPrayer()
    }

    fun retry() {
        loadPrayer()
    }

    private fun loadPrayer() {
        viewModelScope.launch {
            _uiState.value = PrayerDetailUiState.Loading
            if (prayerId.isBlank()) {
                Timber.w("prayerId is blank, showing error")
                _uiState.value = PrayerDetailUiState.Error("Prayer not found")
                return@launch
            }

            val prayer = getPrayerById(prayerId)
            Timber.d("getPrayerById('$prayerId') returned: ${prayer?.title ?: "null"}")
            _uiState.value = if (prayer != null) {
                PrayerDetailUiState.Loaded(prayer)
            } else {
                Timber.e("Prayer not found for ID: $prayerId")
                PrayerDetailUiState.Error("Prayer not found")
            }
        }
    }
}
