package ru.student.cityevents.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.ui.model.TicketsUiEvent
import ru.student.cityevents.ui.model.TicketsUiState
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(private val repository: EventsRepository) : ViewModel() {
    val uiState = repository.observeTickets().map(::TicketsUiState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TicketsUiState())

    fun onEvent(event: TicketsUiEvent) {
        when (event) {
            is TicketsUiEvent.DeleteClicked -> viewModelScope.launch { repository.deleteTicket(event.ticketId) }
        }
    }
}
