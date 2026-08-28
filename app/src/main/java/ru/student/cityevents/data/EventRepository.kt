package ru.student.cityevents.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.student.cityevents.BuildConfig
import ru.student.cityevents.data.local.EventDao
import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketEntity
import ru.student.cityevents.data.local.TicketWithEvent
import ru.student.cityevents.data.remote.ApiEvent
import ru.student.cityevents.data.remote.TicketmasterApi
import javax.inject.Inject
import javax.inject.Singleton

interface EventsRepository {
    fun observeEvents(query: String): Flow<List<EventEntity>>
    fun observeFavorites(): Flow<List<EventEntity>>
    fun observeEvent(id: String): Flow<EventEntity?>
    fun observeTickets(): Flow<List<TicketWithEvent>>
    suspend fun refresh(city: String): Result<Int>
    suspend fun toggleFavorite(id: String)
    suspend fun buyTicket(eventId: String, quantity: Int, totalPrice: Double?)
    suspend fun deleteTicket(ticketId: Long)
}

@Singleton
class OfflineFirstEventsRepository @Inject constructor(
    private val api: TicketmasterApi,
    private val dao: EventDao
) : EventsRepository {
    override fun observeEvents(query: String): Flow<List<EventEntity>> =
        if (query.isBlank()) dao.observeEvents() else dao.searchEvents(query.trim())

    override fun observeFavorites() = dao.observeFavorites()
    override fun observeEvent(id: String) = dao.observeEvent(id)
    override fun observeTickets() = dao.observeTickets()

    override suspend fun refresh(city: String): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            check(BuildConfig.TICKETMASTER_API_KEY.isNotBlank()) {
                "Добавьте TICKETMASTER_API_KEY в local.properties"
            }
            val favorites = dao.favoriteIds().toSet()
            val events = api.getEvents(BuildConfig.TICKETMASTER_API_KEY, city)._embedded?.events.orEmpty()
                .map { it.toEntity(it.id in favorites) }
            dao.upsertEvents(events)
            events.size
        }
    }

    override suspend fun toggleFavorite(id: String) = withContext(Dispatchers.IO) {
        dao.toggleFavorite(id)
    }

    override suspend fun buyTicket(eventId: String, quantity: Int, totalPrice: Double?) =
        withContext(Dispatchers.IO) {
            dao.insertTicket(TicketEntity(eventId = eventId, quantity = quantity, totalPrice = totalPrice))
        }

    override suspend fun deleteTicket(ticketId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTicket(ticketId)
    }
}

private fun ApiEvent.toEntity(favorite: Boolean): EventEntity {
    val venue = _embedded?.venues?.firstOrNull()
    val classification = classifications.firstOrNull()
    val price = priceRanges.firstOrNull()
    return EventEntity(
        id = id,
        name = name,
        imageUrl = images.maxByOrNull { it.width }?.url,
        date = dates.start.localDate,
        time = dates.start.localTime,
        venue = venue?.name ?: "Площадка уточняется",
        city = venue?.city?.name ?: "Город не указан",
        category = classification?.genre?.name ?: classification?.segment?.name ?: "Событие",
        minPrice = price?.min,
        maxPrice = price?.max,
        currency = price?.currency,
        ticketUrl = url,
        isFavorite = favorite
    )
}
