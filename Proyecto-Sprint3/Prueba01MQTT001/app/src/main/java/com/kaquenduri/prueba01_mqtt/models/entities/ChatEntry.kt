package com.kaquenduri.prueba01_mqtt.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_entries")
data class ChatEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String?,
    val timestampMillis: Long
)


