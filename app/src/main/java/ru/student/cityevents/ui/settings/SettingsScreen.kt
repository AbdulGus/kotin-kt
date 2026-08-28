package ru.student.cityevents.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.student.cityevents.ui.model.SettingsUiEvent
import ru.student.cityevents.ui.model.SettingsUiState

@Composable
fun SettingsScreen(state: SettingsUiState, onEvent: (SettingsUiEvent) -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbar.showSnackbar("Настройки сохранены. Обновите каталог.")
            onEvent(SettingsUiEvent.SavedMessageShown)
        }
    }
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.city,
            onValueChange = { onEvent(SettingsUiEvent.CityChanged(it)) },
            label = { Text("Город латиницей") },
            supportingText = { Text("Например: Berlin, London или Paris") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Тёмная тема", style = MaterialTheme.typography.titleMedium)
            Switch(state.darkTheme, { onEvent(SettingsUiEvent.DarkThemeChanged(it)) })
        }
        Button(onClick = { onEvent(SettingsUiEvent.SaveClicked) }, modifier = Modifier.fillMaxWidth()) { Text("Сохранить") }
        Text("Город и тема сохраняются через DataStore.", style = MaterialTheme.typography.bodySmall)
        SnackbarHost(snackbar)
    }
}
