package ru.student.cityevents.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.settings.SettingsRepository
import ru.student.cityevents.ui.model.SettingsUiEvent
import ru.student.cityevents.ui.model.SettingsUiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: SettingsRepository) : ViewModel() {
    val uiState = repository.settings.map { SettingsUiState(it.darkTheme) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onEvent(event: SettingsUiEvent) {
        when (event) {
            is SettingsUiEvent.DarkThemeChanged -> viewModelScope.launch { repository.setDarkTheme(event.enabled) }
        }
    }
}
