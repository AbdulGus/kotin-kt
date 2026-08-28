package ru.student.cityevents.ui.tickets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.student.cityevents.ui.components.EmptyState
import ru.student.cityevents.ui.components.formatDate
import ru.student.cityevents.ui.model.TicketsUiEvent
import ru.student.cityevents.ui.model.TicketsUiState
import java.text.DateFormat
import java.util.Date

@Composable
fun TicketsScreen(state: TicketsUiState, onEvent: (TicketsUiEvent) -> Unit) {
    if (state.tickets.isEmpty()) {
        EmptyState("Билетов пока нет", "Оформленные в приложении билеты появятся здесь")
        return
    }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Мои билеты", style = MaterialTheme.typography.headlineMedium) }
        items(state.tickets, key = { it.ticket.id }) { item ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(item.event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("${formatDate(item.event.date)} • ${item.event.venue}")
                        Text("Билетов: ${item.ticket.quantity}")
                        item.ticket.totalPrice?.let { Text("Сумма: ${it.toInt()} ${item.event.currency.orEmpty()}") }
                        Text("Оформлено ${DateFormat.getDateInstance().format(Date(item.ticket.purchasedAt))}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { onEvent(TicketsUiEvent.DeleteClicked(item.ticket.id)) }) {
                        Icon(Icons.Outlined.Delete, "Удалить билет")
                    }
                }
            }
        }
    }
}
