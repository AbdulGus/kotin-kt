package ru.student.cityevents.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface TicketmasterApi {
    @GET("discovery/v2/events.json")
    suspend fun getEvents(
        @Query("apikey") apiKey: String,
        @Query("city") city: String,
        @Query("size") size: Int = 50,
        @Query("sort") sort: String = "date,asc"
    ): EventsResponse
}

data class EventsResponse(
    val _embedded: EmbeddedEvents? = null,
    val page: PageInfo? = null
)

data class EmbeddedEvents(val events: List<ApiEvent> = emptyList())
data class PageInfo(val totalElements: Int = 0)

data class ApiEvent(
    val id: String,
    val name: String,
    val url: String? = null,
    val images: List<ApiImage> = emptyList(),
    val dates: ApiDates,
    val _embedded: ApiEmbedded? = null,
    val classifications: List<ApiClassification> = emptyList(),
    val priceRanges: List<ApiPriceRange> = emptyList()
)

data class ApiImage(val url: String, val width: Int = 0)
data class ApiDates(val start: ApiStart)
data class ApiStart(val localDate: String, val localTime: String? = null)
data class ApiEmbedded(val venues: List<ApiVenue> = emptyList())
data class ApiVenue(val name: String, val city: ApiCity? = null)
data class ApiCity(val name: String)
data class ApiClassification(val segment: ApiName? = null, val genre: ApiName? = null)
data class ApiName(val name: String)
data class ApiPriceRange(val min: Double? = null, val max: Double? = null, val currency: String? = null)
