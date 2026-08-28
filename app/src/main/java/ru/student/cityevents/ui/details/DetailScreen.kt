package ru.student.cityevents.ui.details

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import ru.student.cityevents.ui.components.EmptyState
import ru.student.cityevents.ui.components.formatDate
import ru.student.cityevents.ui.model.DetailUiEvent
import ru.student.cityevents.ui.model.DetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailUiState, onEvent: (DetailUiEvent) -> Unit, onBack: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    var quantity by remember { mutableIntStateOf(1) }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            onEvent(DetailUiEvent.MessageShown)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("О событии") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val event = state.event
        if (event == null) {
            EmptyState("Событие не найдено", "Возможно, оно было удалено из локального кэша", Modifier.padding(padding))
            return@Scaffold
        }
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            AsyncImage(event.imageUrl, event.name, Modifier.fillMaxWidth().height(260.dp), contentScale = ContentScale.Crop)
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(event.category, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = { onEvent(DetailUiEvent.FavoriteClicked) }) {
                        Icon(if (event.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Избранное")
                    }
                }
                Text(event.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${formatDate(event.date)} • ${event.time?.take(5) ?: "время уточняется"}")
                Text("${event.venue}\n${event.city}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    event.minPrice?.let { "Цена: от ${it.toInt()} ${event.currency.orEmpty()}" } ?: "Цена появится у организатора",
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Количество", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { if (quantity > 1) quantity-- }) { Text("−") }
                    Text(quantity.toString(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                    OutlinedButton(onClick = { if (quantity < 6) quantity++ }) { Text("+") }
                }
                Button(
                    onClick = { onEvent(DetailUiEvent.BuyClicked(quantity)) },
                    enabled = !state.buying,
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (state.buying) "Оформляем…" else "Купить билет") }
                event.ticketUrl?.let { url ->
                    FilledTonalButton(
                        onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Открыть страницу организатора") }
                }
                Spacer(Modifier.height(16.dp))
                Text("Учебная покупка не списывает деньги. Информация о событии получена через Ticketmaster Discovery API.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
