package com.example.agrocode.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrocode.data.repository.SensorRepository
import com.example.agrocode.data.repository.SensorRepositoryImpl
import com.example.agrocode.domain.model.AccionUsuario
import com.example.agrocode.domain.model.Alerta
import com.example.agrocode.domain.model.Lectura
import com.example.agrocode.domain.model.Sensor
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.agrocode.presentation.notifications.Notifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EstadoHomeUI(
    val sensores: List<Sensor> = emptyList(),
    val seleccionados: Set<String> = emptySet(),
    val lecturasRecientes: Map<String, List<Lectura>> = emptyMap(),
    val promedioDiario: Map<String, Float> = emptyMap(),
    val alertasCriticas: List<Alerta> = emptyList(),
    val historialAcciones: List<AccionUsuario> = emptyList(),
    val intervaloMs: Long = 3000L,
    val cargando: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val repositorio: SensorRepository = SensorRepositoryImpl()
) : ViewModel() {

    private val _estadoUI = MutableStateFlow(EstadoHomeUI())
    val estadoUI: StateFlow<EstadoHomeUI> = _estadoUI.asStateFlow()

    init {
        observarDatos()
    }

    private fun observarDatos() {
        viewModelScope.launch {
            repositorio.sensores.collectLatest { lista ->
                _estadoUI.update { it.copy(sensores = lista) }
            }
        }
        viewModelScope.launch {
            repositorio.historialLecturas.collectLatest { mapa ->
                _estadoUI.update {
                    it.copy(
                        lecturasRecientes = mapa,
                        promedioDiario = calcularPromedios(mapa)
                    )
                }
            }
        }
        viewModelScope.launch {
            repositorio.historialAcciones.collectLatest { acciones ->
                _estadoUI.update { it.copy(historialAcciones = acciones) }
            }
        }
        viewModelScope.launch {
            repositorio.intervaloMs.collectLatest { interval ->
                _estadoUI.update { it.copy(intervaloMs = interval) }
            }
        }
        // Iniciar flujo de lecturas y alertas
        viewModelScope.launch {
            repositorio.lecturas.collect { /* side-effect: historial ya se actualiza en repo */ }
        }
        viewModelScope.launch {
            val alertas = mutableListOf<Alerta>()
            repositorio.alertas.collectLatest { alerta ->
                if (alerta.esCritica) {
                    alertas.add(alerta)
                    if (alertas.size > 50 && alertas.isNotEmpty()) alertas.removeAt(0)
                    _estadoUI.update { it.copy(alertasCriticas = alertas.toList()) }
                    // Nota: Notificación se hará desde la UI para evitar dependencia de contexto aquí
                }
            }
        }
    }

    private fun calcularPromedios(mapa: Map<String, List<Lectura>>): Map<String, Float> {
        val limiteTiempo = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        val resultado = mutableMapOf<String, Float>()
        for ((sensorId, lista) in mapa) {
            val recientes = lista.filter { it.timestamp >= limiteTiempo }
            if (recientes.isNotEmpty()) {
                val promedio = when {
                    recientes.any { it.humedadPorcentaje != null } -> recientes.mapNotNull { it.humedadPorcentaje?.toFloat() }.average().toFloat()
                    recientes.any { it.temperaturaCelsius != null } -> recientes.mapNotNull { it.temperaturaCelsius }.average().toFloat()
                    else -> 0f
                }
                resultado[sensorId] = promedio
            }
        }
        return resultado
    }

    fun seleccionar(ids: Set<String>) {
        _estadoUI.update { it.copy(seleccionados = ids) }
        repositorio.seleccionarSensores(ids)
    }

    fun toggleSensor(id: String, activar: Boolean) {
        if (activar) repositorio.activarSensor(id) else repositorio.desactivarSensor(id)
    }

    fun cambiarIntervalo(ms: Long) {
        repositorio.cambiarIntervalo(ms)
    }
}

