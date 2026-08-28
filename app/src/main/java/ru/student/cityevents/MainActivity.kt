package ru.student.cityevents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.student.cityevents.data.settings.SettingsRepository
import ru.student.cityevents.ui.catalog.CatalogScreen
import ru.student.cityevents.ui.catalog.CatalogViewModel
import ru.student.cityevents.ui.details.DetailScreen
import ru.student.cityevents.ui.details.DetailViewModel
import ru.student.cityevents.ui.favorites.FavoritesScreen
import ru.student.cityevents.ui.favorites.FavoritesViewModel
import ru.student.cityevents.ui.settings.SettingsScreen
import ru.student.cityevents.ui.settings.SettingsViewModel
import ru.student.cityevents.ui.theme.CityEventsTheme
import ru.student.cityevents.ui.tickets.TicketsScreen
import ru.student.cityevents.ui.tickets.TicketsViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var settingsRepository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settings by settingsRepository.settings.collectAsState(initial = null)
            CityEventsTheme(darkTheme = settings?.darkTheme == true) { CityEventsApp() }
        }
    }
}

private sealed class MainDestination(val route: String, val label: String, val icon: ImageVector) {
    data object Catalog : MainDestination("catalog", "Афиша", Icons.Outlined.Event)
    data object Favorites : MainDestination("favorites", "Избранное", Icons.Outlined.FavoriteBorder)
    data object Tickets : MainDestination("tickets", "Билеты", Icons.Outlined.ConfirmationNumber)
    data object Settings : MainDestination("settings", "Настройки", Icons.Outlined.Settings)
}

@Composable
private fun CityEventsApp() {
    val navController = rememberNavController()
    val destinations = listOf(MainDestination.Catalog, MainDestination.Favorites, MainDestination.Tickets, MainDestination.Settings)
    val entry by navController.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route != "details/{eventId}"

    Scaffold(bottomBar = {
        if (showBar) NavigationBar {
            destinations.forEach { destination ->
                NavigationBarItem(
                    selected = route == destination.route,
                    onClick = {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(destination.icon, destination.label) },
                    label = { Text(destination.label) }
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = MainDestination.Catalog.route, Modifier.padding(padding)) {
            composable(MainDestination.Catalog.route) {
                val vm: CatalogViewModel = hiltViewModel()
                CatalogScreen(vm.uiState.collectAsStateWithLifecycle().value, vm::onEvent) { navController.navigate("details/$it") }
            }
            composable(MainDestination.Favorites.route) {
                val vm: FavoritesViewModel = hiltViewModel()
                FavoritesScreen(vm.uiState.collectAsStateWithLifecycle().value, vm::onEvent) { navController.navigate("details/$it") }
            }
            composable(MainDestination.Tickets.route) {
                val vm: TicketsViewModel = hiltViewModel()
                TicketsScreen(vm.uiState.collectAsStateWithLifecycle().value, vm::onEvent)
            }
            composable(MainDestination.Settings.route) {
                val vm: SettingsViewModel = hiltViewModel()
                SettingsScreen(vm.uiState.collectAsStateWithLifecycle().value, vm::onEvent)
            }
            composable("details/{eventId}") {
                val vm: DetailViewModel = hiltViewModel()
                DetailScreen(vm.uiState.collectAsStateWithLifecycle().value, vm::onEvent) { navController.popBackStack() }
            }
        }
    }
}
