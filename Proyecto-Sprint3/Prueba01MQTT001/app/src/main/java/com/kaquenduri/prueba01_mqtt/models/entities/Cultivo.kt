package com.kaquenduri.prueba01_mqtt.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cultivos")
data class Cultivo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val descripcion: String,
    val tipo: String,
    val especie: String,
    val fechaSiembra: String,
    val idUsuario: Int,
    // SENSORES REALES (Firebase/MQTT)
    val sensorHumedadSuelo: Boolean = false,
    val sensorTemperatura: Boolean = false,
    val sensorHumedadAire: Boolean = false,
    // SENSORES SIMULADOS (Generación automática)
    val sensorPh: Boolean = false,
    val sensorConductividad: Boolean = false,
    val sensorNutrientes: Boolean = false,
    val sensorLuz: Boolean = false
)
