package com.kaquenduri.prueba01_mqtt.models.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kaquenduri.prueba01_mqtt.models.entities.ChatEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ChatEntry): Long

    @Query("SELECT * FROM chat_entries WHERE timestampMillis >= :minTime ORDER BY timestampMillis DESC")
    fun entriesSince(minTime: Long): Flow<List<ChatEntry>>

    @Query("DELETE FROM chat_entries WHERE timestampMillis < :minTime")
    suspend fun deleteOlderThan(minTime: Long)

    @Query("DELETE FROM chat_entries")
    suspend fun clearAll()
}


