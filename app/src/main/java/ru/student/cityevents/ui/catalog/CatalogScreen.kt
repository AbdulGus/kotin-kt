package ru.student.cityevents.ui.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.student.cityevents.ui.components.EmptyState
import ru.student.cityevents.ui.components.EventCard
import ru.student.cityevents.ui.model.CatalogUiEvent
import ru.student.cityevents.ui.model.CatalogUiState
import ru.student.cityevents.ui.model.LoadStatus

@Composable
fun CatalogScreen(state: CatalogUiState, onEvent: (CatalogUiEvent) -> Unit, onOpenEvent: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Куда сходить", style = MaterialTheme.typography.headlineMedium)
            Text("Москва", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text("Концерты, выставки и городские события", style = MaterialTheme.typography.bodyMedium)
        }
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(CatalogUiEvent.SearchChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                label = { Text("Название, площадка или жанр") }
            )
        }
        if (state.status is LoadStatus.Loading && state.events.isNotEmpty()) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }
        if (state.status is LoadStatus.Error) {
            item {
                Text(state.status.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { onEvent(CatalogUiEvent.Refresh) }, modifier = Modifier.padding(top = 8.dp)) { Text("Повторить") }
            }
        }
        if (state.events.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxHeight(0.65f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    if (state.status is LoadStatus.Loading) CircularProgressIndicator()
                    else EmptyState(
                        if (state.query.isBlank()) "Событий пока нет" else "Ничего не найдено",
                        if (state.query.isBlank()) "Проверьте интернет и попробуйте обновить афишу" else "Попробуйте изменить запрос"
                    )
                }
            }
        } else {
            items(state.events, key = { it.id }) { event ->
                EventCard(event, { onOpenEvent(event.id) }, { onEvent(CatalogUiEvent.FavoriteClicked(event.id)) })
            }
        }
    }
}
