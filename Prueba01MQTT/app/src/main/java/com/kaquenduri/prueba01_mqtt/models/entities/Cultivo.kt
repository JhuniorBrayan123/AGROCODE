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
    val idUsuario: Int
)
