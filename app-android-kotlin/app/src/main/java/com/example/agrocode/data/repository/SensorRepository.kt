package com.example.agrocode.data.repository

import com.example.agrocode.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

interface SensorRepository {
    val sensores: StateFlow<List<Sensor>>
    val lecturas: Flow<Lectura>
    val alertas: Flow<Alerta>
    val historialLecturas: StateFlow<Map<String, List<Lectura>>>
    val historialAcciones: StateFlow<List<AccionUsuario>>
    val intervaloMs: StateFlow<Long>

    fun seleccionarSensores(ids: Set<String>)
    fun activarSensor(id: String)
    fun desactivarSensor(id: String)
    fun cambiarIntervalo(nuevoIntervaloMs: Long)
}

class SensorRepositoryImpl : SensorRepository {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _sensores = MutableStateFlow(
        listOf(
            Sensor("hum1", "Humedad 1", TipoSensor.HUMEDAD, activo = true),
            Sensor("hum2", "Humedad 2", TipoSensor.HUMEDAD, activo = true),
            Sensor("tmp1", "Temp 1", TipoSensor.TEMPERATURA, activo = true)
        )
    )
    override val sensores: StateFlow<List<Sensor>> = _sensores.asStateFlow()

    private val _historialLecturas = MutableStateFlow<Map<String, List<Lectura>>>(emptyMap())
    override val historialLecturas: StateFlow<Map<String, List<Lectura>>> = _historialLecturas.asStateFlow()

    private val _historialAcciones = MutableStateFlow<List<AccionUsuario>>(emptyList())
    override val historialAcciones: StateFlow<List<AccionUsuario>> = _historialAcciones.asStateFlow()

    private val _intervaloMs = MutableStateFlow(3000L)
    override val intervaloMs: StateFlow<Long> = _intervaloMs.asStateFlow()

    private val seleccionados = MutableStateFlow<Set<String>>(emptySet())

    override val lecturas: Flow<Lectura> = flow {
        while (true) {
            val activos = _sensores.value.filter { it.activo }
            val idsAEmitir = if (seleccionados.value.isNotEmpty()) activos.filter { it.id in seleccionados.value } else activos
            val ahora = System.currentTimeMillis()
            for (sensor in idsAEmitir) {
                val lectura = when (sensor.tipo) {
                    TipoSensor.HUMEDAD -> Lectura(sensor.id, ahora, humedadPorcentaje = Random.nextInt(40, 90), temperaturaCelsius = null)
                    TipoSensor.TEMPERATURA -> Lectura(sensor.id, ahora, humedadPorcentaje = null, temperaturaCelsius = Random.nextInt(18, 35).toFloat())
                }
                registrarLectura(lectura)
                emit(lectura)
                evaluarAlertas(lectura)
                delay(50)
            }
            delay(_intervaloMs.value)
        }
    }

    override val alertas: Flow<Alerta> = flow {
        // Las alertas se emiten al evaluarLecturas; para simplificar, reevaluamos del historial reciente
        while (true) {
            val recientes = _historialLecturas.value.values.flatten().takeLast(5)
            val ahora = System.currentTimeMillis()
            for (l in recientes) {
                val alerta = crearAlertaSiCritica(l, ahora)
                if (alerta != null) emit(alerta)
            }
            delay(2000)
        }
    }

    private fun registrarLectura(lectura: Lectura) {
        _historialLecturas.update { mapa ->
            val lista = (mapa[lectura.sensorId] ?: emptyList()) + lectura
            val recortada = if (lista.size > 200) lista.takeLast(200) else lista
            mapa + (lectura.sensorId to recortada)
        }
    }

    private fun evaluarAlertas(lectura: Lectura) {
        crearAlertaSiCritica(lectura, System.currentTimeMillis())
        // En esta implementación simple no almacenamos un stream; la UI puede observar alertas Flow
    }

    private fun crearAlertaSiCritica(lectura: Lectura, ahora: Long): Alerta? {
        val alerta = when {
            lectura.humedadPorcentaje != null && (lectura.humedadPorcentaje < 45 || lectura.humedadPorcentaje > 85) ->
                Alerta(lectura.sensorId, ahora, esCritica = true, mensaje = "Humedad fuera de rango: ${lectura.humedadPorcentaje}%")
            lectura.temperaturaCelsius != null && (lectura.temperaturaCelsius < 18f || lectura.temperaturaCelsius > 32f) ->
                Alerta(lectura.sensorId, ahora, esCritica = true, mensaje = "Temperatura fuera de rango: ${lectura.temperaturaCelsius}°C")
            else -> null
        }
        return alerta
    }

    override fun seleccionarSensores(ids: Set<String>) {
        seleccionados.value = ids
        registrarAccion(TipoAccion.SELECCIONAR_SENSORES, ids.joinToString(","))
    }

    override fun activarSensor(id: String) {
        _sensores.update { lista -> lista.map { if (it.id == id) it.copy(activo = true) else it } }
        registrarAccion(TipoAccion.ACTIVAR_SENSOR, id)
    }

    override fun desactivarSensor(id: String) {
        _sensores.update { lista -> lista.map { if (it.id == id) it.copy(activo = false) else it } }
        registrarAccion(TipoAccion.DESACTIVAR_SENSOR, id)
    }

    override fun cambiarIntervalo(nuevoIntervaloMs: Long) {
        _intervaloMs.value = nuevoIntervaloMs
        registrarAccion(TipoAccion.CAMBIAR_INTERVALO, nuevoIntervaloMs.toString())
    }

    private fun registrarAccion(tipo: TipoAccion, detalle: String) {
        _historialAcciones.update { it + AccionUsuario(System.currentTimeMillis(), tipo, detalle) }
    }
}




