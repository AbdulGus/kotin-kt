package ru.student.cityevents.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.student.cityevents.ui.model.SettingsUiEvent
import ru.student.cityevents.ui.model.SettingsUiState

@Composable
fun SettingsScreen(state: SettingsUiState, onEvent: (SettingsUiEvent) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Тёмная тема", style = MaterialTheme.typography.titleMedium)
            Switch(state.darkTheme, { onEvent(SettingsUiEvent.DarkThemeChanged(it)) })
        }
        Text("О приложении", style = MaterialTheme.typography.titleLarge)
        Text("«Афиша Лондона» помогает найти интересные события, сохранить их в избранное и оформить учебный билет.")
        Text("Источник данных — открытый API StungEvents. Список мероприятий сохраняется на устройстве и доступен без интернета.")
        Text("Версия 1.2", style = MaterialTheme.typography.bodySmall)
    }
}
