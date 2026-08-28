package ru.student.cityevents.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.student.cityevents.ui.components.EmptyState
import ru.student.cityevents.ui.components.EventCard
import ru.student.cityevents.ui.model.FavoritesUiEvent
import ru.student.cityevents.ui.model.FavoritesUiState

@Composable
fun FavoritesScreen(state: FavoritesUiState, onEvent: (FavoritesUiEvent) -> Unit, onOpenEvent: (String) -> Unit) {
    if (state.events.isEmpty()) {
        EmptyState("Избранное пусто", "Нажмите на сердечко в каталоге, чтобы сохранить событие")
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Избранное", style = MaterialTheme.typography.headlineMedium) }
        items(state.events, key = { it.id }) { event ->
            EventCard(event, { onOpenEvent(event.id) }, { onEvent(FavoritesUiEvent.FavoriteClicked(event.id)) })
        }
    }
}
