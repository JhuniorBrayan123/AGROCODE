package com.kaquenduri.prueba01_mqtt.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "alertas")
data class Alerta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mensaje: String,
    val fechaHora: Long = System.currentTimeMillis()
)
