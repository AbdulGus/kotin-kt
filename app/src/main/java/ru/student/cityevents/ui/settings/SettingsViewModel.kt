package ru.student.cityevents.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.settings.SettingsRepository
import ru.student.cityevents.ui.model.SettingsUiEvent
import ru.student.cityevents.ui.model.SettingsUiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    private val draftCity = MutableStateFlow<String?>(null)
    private val saved = MutableStateFlow(false)

    val uiState = combine(repository.settings, draftCity, saved) { settings, draft, isSaved ->
        SettingsUiState(draft ?: settings.city, settings.darkTheme, isSaved)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.CityChanged -> draftCity.value = event.value
            is SettingsUiEvent.DarkThemeChanged -> viewModelScope.launch { repository.setDarkTheme(event.enabled) }
            SettingsUiEvent.SaveClicked -> viewModelScope.launch {
                repository.saveCity(uiState.value.city)
                draftCity.value = null
                saved.value = true
            }
            SettingsUiEvent.SavedMessageShown -> saved.value = false
        }
    }
}
