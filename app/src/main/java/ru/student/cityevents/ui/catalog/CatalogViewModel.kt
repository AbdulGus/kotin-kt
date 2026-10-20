package ru.student.cityevents.ui.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.ui.model.CatalogUiEvent
import ru.student.cityevents.ui.model.CatalogUiState
import ru.student.cityevents.ui.model.LoadStatus
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModel @Inject constructor(
    private val repository: EventsRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val status = MutableStateFlow<LoadStatus>(LoadStatus.Loading)
    private val events = query.flatMapLatest(repository::observeEvents)

    val uiState = combine(events, query, status) { items, search, load ->
        CatalogUiState(items, search, load)
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
        repository.refresh()
            .onSuccess { status.value = LoadStatus.Idle }
            .onFailure {
                status.value = LoadStatus.Error("Не удалось обновить афишу. Проверьте интернет и попробуйте ещё раз.")
            }
    }
}
