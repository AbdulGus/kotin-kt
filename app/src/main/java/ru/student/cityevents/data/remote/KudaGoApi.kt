package ru.student.cityevents.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface KudaGoApi {
    @GET("public-api/v1.4/events/")
    suspend fun getMoscowEvents(
        @Query("location") location: String = "msk",
        @Query("lang") language: String = "ru",
        @Query("page_size") pageSize: Int = 60,
        @Query("actual_since") actualSince: Long,
        @Query("fields") fields: String = FIELDS,
        @Query("expand") expand: String = "place",
        @Query("text_format") textFormat: String = "text",
        @Query("order_by") orderBy: String = "-publication_date"
    ): EventsResponse

    companion object {
        const val FIELDS = "id,title,place,dates,images,description,site_url,categories,price,age_restriction"
    }
}

data class EventsResponse(
    val count: Int = 0,
    val next: String? = null,
    val results: List<ApiEvent> = emptyList()
)

data class ApiEvent(
    val id: Long,
    val title: String,
    val place: ApiPlace? = null,
    val dates: List<ApiDate> = emptyList(),
    val images: List<ApiImage> = emptyList(),
    val description: String? = null,
    val site_url: String? = null,
    val categories: List<String> = emptyList(),
    val price: String? = null,
    val age_restriction: String? = null
)

data class ApiDate(val start: Long, val end: Long)
data class ApiImage(val image: String)
data class ApiPlace(
    val id: Long,
    val title: String,
    val address: String = "",
    val subway: String = ""
)
