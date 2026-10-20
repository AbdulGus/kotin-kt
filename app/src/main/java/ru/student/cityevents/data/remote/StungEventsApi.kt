package ru.student.cityevents.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

interface StungEventsApi {
    @GET("events")
    suspend fun getEvents(
        @Query("city") city: String = "London",
        @Query("limit") limit: Int = 60
    ): EventsResponse
}

data class EventsResponse(
    val ok: Boolean = false,
    val events: List<ApiEvent> = emptyList()
)

data class ApiEvent(
    val id: String,
    val title: String,
    val description: String? = null,
    @SerializedName("start_utc") val startUtc: String? = null,
    @SerializedName("venue_name") val venueName: String? = null,
    val city: String? = null,
    val category: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("ticket_url") val ticketUrl: String? = null,
    @SerializedName("ticket_price_min") val minPrice: Double? = null,
    @SerializedName("ticket_price_max") val maxPrice: Double? = null,
    @SerializedName("ticket_currency") val currency: String? = null
)
