package ru.student.cityevents.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.student.cityevents.data.local.EventDao
import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketEntity
import ru.student.cityevents.data.local.TicketWithEvent
import ru.student.cityevents.data.remote.ApiEvent
import ru.student.cityevents.data.remote.StungEventsApi
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

interface EventsRepository {
    fun observeEvents(query: String): Flow<List<EventEntity>>
    fun observeFavorites(): Flow<List<EventEntity>>
    fun observeEvent(id: String): Flow<EventEntity?>
    fun observeTickets(): Flow<List<TicketWithEvent>>
    suspend fun refresh(): Result<Int>
    suspend fun toggleFavorite(id: String)
    suspend fun buyTicket(eventId: String, quantity: Int, totalPrice: Double?)
    suspend fun deleteTicket(ticketId: Long)
}

@Singleton
class OfflineFirstEventsRepository @Inject constructor(
    private val api: StungEventsApi,
    private val dao: EventDao
) : EventsRepository {
    override fun observeEvents(query: String): Flow<List<EventEntity>> =
        if (query.isBlank()) dao.observeEvents() else dao.searchEvents(
            query.trim().replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
        )

    override fun observeFavorites() = dao.observeFavorites()
    override fun observeEvent(id: String) = dao.observeEvent(id)
    override fun observeTickets() = dao.observeTickets()

    override suspend fun refresh(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getEvents()
            check(response.ok) { "Сервис событий вернул ошибку" }
            val favorites = dao.favoriteIds().toSet()
            val events = response.events
                .map { it.toEntity(it.id in favorites) }
            check(events.isNotEmpty()) { "На сервере пока нет событий Лондона" }
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
    val listedPrice = minPrice?.takeIf { it > 0 }
    val dateTime = startUtc?.let { value ->
        runCatching { Instant.parse(value).atZone(LONDON_ZONE) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).atZoneSameInstant(LONDON_ZONE) }.getOrNull()
            ?: runCatching {
                LocalDateTime.parse(value.replace(' ', 'T'))
                    .atZone(ZoneId.of("UTC"))
                    .withZoneSameInstant(LONDON_ZONE)
            }.getOrNull()
    }
    return EventEntity(
        id = id,
        name = title.replaceFirstChar { it.uppercase() },
        imageUrl = imageUrl,
        date = dateTime?.toLocalDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE)
            ?: startUtc?.take(10)?.takeIf { runCatching { LocalDate.parse(it) }.isSuccess }
            ?: LocalDate.now(LONDON_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE),
        time = dateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")),
        venue = venueName?.takeIf { it.isNotBlank() } ?: "Площадка уточняется",
        city = "Лондон",
        category = category.toRussianCategory(),
        minPrice = listedPrice,
        maxPrice = maxPrice?.takeIf { it > 0 },
        currency = currency?.takeIf { listedPrice != null },
        ticketUrl = ticketUrl,
        description = description.orEmpty().take(500).trim(),
        priceText = listedPrice?.let { "от ${it.toInt()} ${currency.orEmpty()}" },
        isFavorite = favorite
    )
}

private fun String?.toRussianCategory(): String = when (this) {
    "concerts" -> "Концерт"
    "festivals" -> "Фестиваль"
    "sports" -> "Спорт"
    "cinema" -> "Кино"
    "esports" -> "Киберспорт"
    "performing_arts" -> "Театр"
    "outdoors" -> "На открытом воздухе"
    else -> "Событие"
}

private val LONDON_ZONE: ZoneId = ZoneId.of("Europe/London")
