package ru.student.cityevents.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventId")]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val quantity: Int,
    val totalPrice: Double?,
    val purchasedAt: Long = System.currentTimeMillis()
)

data class TicketWithEvent(
    @androidx.room.Embedded val ticket: TicketEntity,
    @androidx.room.Relation(parentColumn = "eventId", entityColumn = "id")
    val event: EventEntity
)
