package ru.student.cityevents.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.ui.model.FavoritesUiEvent
import ru.student.cityevents.ui.model.FavoritesUiState
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(private val repository: EventsRepository) : ViewModel() {
    val uiState = repository.observeFavorites()
        .map(::FavoritesUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    fun onEvent(event: FavoritesUiEvent) {
        when (event) {
            is FavoritesUiEvent.FavoriteClicked -> viewModelScope.launch { repository.toggleFavorite(event.id) }
        }
    }
}
