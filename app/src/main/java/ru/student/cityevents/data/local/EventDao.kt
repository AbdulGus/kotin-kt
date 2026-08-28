package ru.student.cityevents.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY date, time")
    fun observeEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE name LIKE '%' || :query || '%' OR venue LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY date, time")
    fun searchEvents(query: String): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE isFavorite = 1 ORDER BY date, time")
    fun observeFavorites(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE id = :id LIMIT 1")
    fun observeEvent(id: String): Flow<EventEntity?>

    @Query("SELECT id FROM events WHERE isFavorite = 1")
    suspend fun favoriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<EventEntity>)

    @Query("UPDATE events SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Insert
    suspend fun insertTicket(ticket: TicketEntity)

    @Transaction
    @Query("SELECT * FROM tickets ORDER BY purchasedAt DESC")
    fun observeTickets(): Flow<List<TicketWithEvent>>

    @Query("DELETE FROM tickets WHERE id = :ticketId")
    suspend fun deleteTicket(ticketId: Long)
}
