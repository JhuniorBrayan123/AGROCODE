package com.kaquenduri.prueba01_mqtt.models.entities

import androidx.room.Entity
import  androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "registro_actividades",
)
data class RegistroActividad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val cultivoId: Int,
    val fecha: String,
    val tipoActividad: String,
    val descripcion: String,
    val productos : String = "",
    val dosis: String = "",
    val objetivo: String = "",
    val costo: Double = 0.0,
    val jornales: Int = 0,
    val materiales : String = "",
    val plantaObjetivo: String = ""
)


