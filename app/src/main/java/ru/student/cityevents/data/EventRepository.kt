package ru.student.cityevents.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.student.cityevents.data.local.EventDao
import ru.student.cityevents.data.local.EventEntity
import ru.student.cityevents.data.local.TicketEntity
import ru.student.cityevents.data.local.TicketWithEvent
import ru.student.cityevents.data.remote.ApiEvent
import ru.student.cityevents.data.remote.ApiDate
import ru.student.cityevents.data.remote.KudaGoApi
import java.time.Instant
import java.time.LocalDate
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
    private val api: KudaGoApi,
    private val dao: EventDao
) : EventsRepository {
    override fun observeEvents(query: String): Flow<List<EventEntity>> =
        if (query.isBlank()) dao.observeEvents() else dao.searchEvents(query.trim())

    override fun observeFavorites() = dao.observeFavorites()
    override fun observeEvent(id: String) = dao.observeEvent(id)
    override fun observeTickets() = dao.observeTickets()

    override suspend fun refresh(): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val favorites = dao.favoriteIds().toSet()
            val now = Instant.now().epochSecond
            val events = api.getMoscowEvents(actualSince = now).results
                .map { it.toEntity(it.id.toString() in favorites, now) }
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

private fun ApiEvent.toEntity(favorite: Boolean, now: Long): EventEntity {
    val eventDate = dates.bestDate(now)
    val dateTime = eventDate?.start?.takeIf { it >= now }
        ?.let { Instant.ofEpochSecond(it).atZone(MOSCOW_ZONE) }
    val priceNumber = Regex("\\d[\\d ]*").find(price)?.value?.replace(" ", "")?.toDoubleOrNull()
    return EventEntity(
        id = id.toString(),
        name = title.replaceFirstChar { it.uppercase() },
        imageUrl = images.firstOrNull()?.image,
        date = dateTime?.toLocalDate()?.format(DateTimeFormatter.ISO_LOCAL_DATE)
            ?: LocalDate.now(MOSCOW_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE),
        time = dateTime?.toLocalTime()?.format(DateTimeFormatter.ofPattern("HH:mm")),
        venue = place?.title ?: "Площадка уточняется",
        city = "Москва",
        category = categories.firstOrNull().toRussianCategory(),
        minPrice = priceNumber,
        maxPrice = null,
        currency = if (priceNumber != null) "₽" else null,
        ticketUrl = site_url,
        description = description,
        ageRestriction = age_restriction.ifBlank { null },
        priceText = price.ifBlank { null },
        isFavorite = favorite
    )
}

private fun List<ApiDate>.bestDate(now: Long): ApiDate? =
    filter { it.end >= now }
        .minByOrNull { date -> if (date.start >= now) date.start else Long.MAX_VALUE - date.end }

private fun String?.toRussianCategory(): String = when (this) {
    "concert" -> "Концерт"
    "theater" -> "Театр"
    "exhibition" -> "Выставка"
    "festival" -> "Фестиваль"
    "education" -> "Образование"
    "kids" -> "Детям"
    "recreation" -> "Отдых"
    "entertainment" -> "Развлечения"
    "cinema" -> "Кино"
    "party" -> "Вечеринка"
    "sport" -> "Спорт"
    else -> "Событие"
}

private val MOSCOW_ZONE: ZoneId = ZoneId.of("Europe/Moscow")
