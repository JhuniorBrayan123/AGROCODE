package com.kaquenduri.prueba01_mqtt.models.entities

import androidx.room.Entity
import  androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "registro_actividades",
    foreignKeys =[ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["idUsuario"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class RegistroActividad(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val cultivoId: Int,
    val idUsuario: Int,
    val fecha: String,
    val tipoActividad: String,
    val descripcion: String,
    val productos : String="",
    val dosis: String = "", //del pH 15 x bombe
    val objetivo: String ="",//Para el tipo de plaga
    val costo: Double = 0.0,
    val jornales: Int = 0,
    val materiales : String="",
    val plantaObjetivo: String="" //Para las aplicaciones mas especificas
)


