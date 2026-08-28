package ru.student.cityevents.ui.model

import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketWithEvent

sealed interface LoadStatus {
    data object Idle : LoadStatus
    data object Loading : LoadStatus
    data class Error(val message: String) : LoadStatus
}

data class CatalogUiState(
    val events: List<EventEntity> = emptyList(),
    val query: String = "",
    val city: String = "Berlin",
    val status: LoadStatus = LoadStatus.Loading
)

sealed interface CatalogUiEvent {
    data object Refresh : CatalogUiEvent
    data class SearchChanged(val value: String) : CatalogUiEvent
    data class FavoriteClicked(val id: String) : CatalogUiEvent
}

data class FavoritesUiState(val events: List<EventEntity> = emptyList())
sealed interface FavoritesUiEvent { data class FavoriteClicked(val id: String) : FavoritesUiEvent }

data class DetailUiState(
    val event: EventEntity? = null,
    val buying: Boolean = false,
    val message: String? = null
)

sealed interface DetailUiEvent {
    data object FavoriteClicked : DetailUiEvent
    data class BuyClicked(val quantity: Int) : DetailUiEvent
    data object MessageShown : DetailUiEvent
}

data class TicketsUiState(val tickets: List<TicketWithEvent> = emptyList())
sealed interface TicketsUiEvent { data class DeleteClicked(val ticketId: Long) : TicketsUiEvent }

data class SettingsUiState(
    val city: String = "Berlin",
    val darkTheme: Boolean = false,
    val saved: Boolean = false
)

sealed interface SettingsUiEvent {
    data class CityChanged(val value: String) : SettingsUiEvent
    data class DarkThemeChanged(val enabled: Boolean) : SettingsUiEvent
    data object SaveClicked : SettingsUiEvent
    data object SavedMessageShown : SettingsUiEvent
}
