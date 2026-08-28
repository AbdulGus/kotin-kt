package ru.student.cityevents.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF9A2738),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADD),
    secondary = Color(0xFF67565A),
    tertiary = Color(0xFF785831),
    background = Color(0xFFFFF8F7),
    surfaceVariant = Color(0xFFF3DDDF)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFFB2BB),
    primaryContainer = Color(0xFF7B1024),
    secondary = Color(0xFFE7BDC2),
    tertiary = Color(0xFFE9BF8F)
)

@Composable
fun CityEventsTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (darkTheme) DarkColors else LightColors, content = content)
}
