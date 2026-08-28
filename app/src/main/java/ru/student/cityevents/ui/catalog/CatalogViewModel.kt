package ru.student.cityevents.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.data.settings.SettingsRepository
import ru.student.cityevents.ui.model.CatalogUiEvent
import ru.student.cityevents.ui.model.CatalogUiState
import ru.student.cityevents.ui.model.LoadStatus
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel @Inject constructor(
    private val repository: EventsRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val status = MutableStateFlow<LoadStatus>(LoadStatus.Loading)
    private val events = query.flatMapLatest(repository::observeEvents)

    val uiState = combine(events, query, settingsRepository.settings, status) { items, search, settings, load ->
        CatalogUiState(items, search, settings.city, load)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    init { refresh() }

    fun onEvent(event: CatalogUiEvent) {
        when (event) {
            CatalogUiEvent.Refresh -> refresh()
            is CatalogUiEvent.SearchChanged -> query.value = event.value
            is CatalogUiEvent.FavoriteClicked -> viewModelScope.launch { repository.toggleFavorite(event.id) }
        }
    }

    private fun refresh() = viewModelScope.launch {
        status.value = LoadStatus.Loading
        val city = settingsRepository.settings.first().city
        repository.refresh(city)
            .onSuccess { status.value = LoadStatus.Idle }
            .onFailure { error ->
                status.value = LoadStatus.Error(error.message ?: "Не удалось обновить афишу")
            }
    }
}
