package ru.student.cityevents.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,
    val date: String,
    val time: String?,
    val venue: String,
    val city: String,
    val category: String,
    val minPrice: Double?,
    val maxPrice: Double?,
    val currency: String?,
    val ticketUrl: String?,
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
