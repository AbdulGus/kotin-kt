package ru.student.cityevents

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import ru.student.cityevents.data.local.AppDatabase
import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketEntity
import ru.student.cityevents.data.OfflineFirstEventsRepository
import ru.student.cityevents.data.remote.ApiDate
import ru.student.cityevents.data.remote.ApiEvent
import ru.student.cityevents.data.remote.EventsResponse
import ru.student.cityevents.data.remote.KudaGoApi
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class EventDaoTest {
    @Test
    fun ticketRemainsAfterEventRefresh() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = database.eventDao()
            val event = EventEntity(
                id = "42",
                name = "Концерт",
                imageUrl = null,
                date = "2026-10-20",
                time = "19:00",
                venue = "Зал",
                city = "Москва",
                category = "Концерт",
                minPrice = 500.0,
                maxPrice = null,
                currency = "₽",
                ticketUrl = null,
                isFavorite = true
            )
            dao.upsertEvents(listOf(event))
            dao.insertTicket(TicketEntity(eventId = event.id, quantity = 2, totalPrice = 1000.0))

            dao.upsertEvents(listOf(event.copy(name = "Обновлённый концерт")))

            val tickets = dao.observeTickets().first()
            assertEquals(1, tickets.size)
            assertEquals("Обновлённый концерт", tickets.single().event.name)
            assertEquals(2, tickets.single().ticket.quantity)
            assertEquals(true, dao.observeFavorites().first().single().isFavorite)
        } finally {
            database.close()
        }
    }

    @Test
    fun cachedEventsRemainAvailableAfterNetworkError() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val api = FakeKudaGoApi()
            val repository = OfflineFirstEventsRepository(api, database.eventDao())

            assertEquals(true, repository.refresh().isSuccess)
            assertEquals(1, repository.observeEvents("Концерт").first().size)
            assertEquals(0, repository.observeEvents("Неизвестное").first().size)
            assertEquals(0, repository.observeEvents("%").first().size)

            repository.toggleFavorite("42")
            repository.buyTicket("42", 2, 1000.0)
            assertEquals(true, repository.refresh().isSuccess)
            assertEquals(1, repository.observeFavorites().first().size)
            assertEquals(1, repository.observeTickets().first().size)

            api.fail = true
            assertEquals(true, repository.refresh().isFailure)
            assertEquals(1, repository.observeEvents("").first().size)
            assertEquals(1, repository.observeTickets().first().size)
        } finally {
            database.close()
        }
    }
}

private class FakeKudaGoApi : KudaGoApi {
    var fail = false

    override suspend fun getMoscowEvents(
        location: String,
        language: String,
        pageSize: Int,
        actualSince: Long,
        fields: String,
        expand: String,
        textFormat: String,
        orderBy: String
    ): EventsResponse {
        if (fail) error("Network unavailable")
        return EventsResponse(
            results = listOf(
                ApiEvent(
                    id = 42,
                    title = "Концерт",
                    dates = listOf(ApiDate(Instant.now().epochSecond + 3600, Instant.now().epochSecond + 7200)),
                    price = "500 рублей"
                )
            )
        )
    }
}
