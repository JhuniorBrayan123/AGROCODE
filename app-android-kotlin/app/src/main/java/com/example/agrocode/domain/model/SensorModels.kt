package com.example.agrocode.domain.model

enum class TipoSensor { HUMEDAD, TEMPERATURA }

data class Sensor(
    val id: String,
    val nombre: String,
    val tipo: TipoSensor,
    val activo: Boolean
)

data class Lectura(
    val sensorId: String,
    val timestamp: Long,
    val humedadPorcentaje: Int?,
    val temperaturaCelsius: Float?
)

data class Alerta(
    val sensorId: String,
    val timestamp: Long,
    val esCritica: Boolean,
    val mensaje: String
)

enum class TipoAccion { ACTIVAR_SENSOR, DESACTIVAR_SENSOR, CAMBIAR_INTERVALO, SELECCIONAR_SENSORES }

data class AccionUsuario(
    val timestamp: Long,
    val tipo: TipoAccion,
    val detalle: String
)



