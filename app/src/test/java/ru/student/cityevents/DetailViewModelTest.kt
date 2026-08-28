package ru.student.cityevents

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.student.cityevents.data.EventsRepository
import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketWithEvent
import ru.student.cityevents.ui.details.DetailViewModel
import ru.student.cityevents.ui.model.DetailUiEvent

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `нажатие на избранное передает id события в репозиторий`() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = DetailViewModel(SavedStateHandle(mapOf("eventId" to "event-1")), repository)

        viewModel.onEvent(DetailUiEvent.FavoriteClicked)
        advanceUntilIdle()

        assertEquals("event-1", repository.favoriteId)
    }

    @Test
    fun `при покупке сохраняется количество и рассчитанная сумма`() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = DetailViewModel(SavedStateHandle(mapOf("eventId" to "event-1")), repository)

        viewModel.uiState.test {
            awaitItem()
            awaitItem()
            viewModel.onEvent(DetailUiEvent.BuyClicked(quantity = 2))
            advanceUntilIdle()

            assertEquals("event-1", repository.boughtEventId)
            assertEquals(2, repository.boughtQuantity)
            assertEquals(50.0, repository.boughtTotal)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

private class FakeRepository : EventsRepository {
    private val event = MutableStateFlow(
        EventEntity(
            id = "event-1", name = "Тестовый концерт", imageUrl = null,
            date = "2026-09-01", time = "19:00:00", venue = "Тестовая площадка",
            city = "Москва", category = "Концерт", minPrice = 25.0,
            maxPrice = 50.0, currency = "EUR", ticketUrl = null
        )
    )
    var favoriteId: String? = null
    var boughtEventId: String? = null
    var boughtQuantity: Int? = null
    var boughtTotal: Double? = null

    override fun observeEvents(query: String): Flow<List<EventEntity>> = MutableStateFlow(listOf(event.value))
    override fun observeFavorites(): Flow<List<EventEntity>> = MutableStateFlow(emptyList())
    override fun observeEvent(id: String): Flow<EventEntity?> = event
    override fun observeTickets(): Flow<List<TicketWithEvent>> = MutableStateFlow(emptyList())
    override suspend fun refresh() = Result.success(1)
    override suspend fun toggleFavorite(id: String) { favoriteId = id }
    override suspend fun buyTicket(eventId: String, quantity: Int, totalPrice: Double?) {
        boughtEventId = eventId
        boughtQuantity = quantity
        boughtTotal = totalPrice
    }
    override suspend fun deleteTicket(ticketId: Long) = Unit
}
