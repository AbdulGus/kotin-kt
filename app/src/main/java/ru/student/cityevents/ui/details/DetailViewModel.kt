package ru.student.cityevents.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.ui.model.DetailUiEvent
import ru.student.cityevents.ui.model.DetailUiState
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EventsRepository
) : ViewModel() {
    private val eventId: String = checkNotNull(savedStateHandle["eventId"])
    private val buying = MutableStateFlow(false)
    private val message = MutableStateFlow<String?>(null)

    val uiState = combine(repository.observeEvent(eventId), buying, message) { event, isBuying, text ->
        DetailUiState(event, isBuying, text)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun onEvent(event: DetailUiEvent) {
        when (event) {
            DetailUiEvent.FavoriteClicked -> viewModelScope.launch { repository.toggleFavorite(eventId) }
            DetailUiEvent.MessageShown -> message.value = null
            is DetailUiEvent.BuyClicked -> buy(event.quantity)
        }
    }

    private fun buy(quantity: Int) = viewModelScope.launch {
        val event = uiState.value.event ?: return@launch
        buying.value = true
        runCatching {
            repository.buyTicket(event.id, quantity, event.minPrice?.times(quantity))
        }.onSuccess {
            message.value = "Билет сохранён в разделе «Мои билеты»"
        }.onFailure {
            message.value = "Не удалось оформить билет"
        }
        buying.value = false
    }
}
