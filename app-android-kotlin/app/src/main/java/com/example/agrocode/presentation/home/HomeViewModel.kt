package com.example.agrocode.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LecturaSensor(
    val humedadPorcentaje: Int,
    val temperaturaCelsius: Float
)

data class EstadoHomeUI(
    val lecturaSensor: LecturaSensor? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    
    private val _estadoUI = MutableStateFlow(EstadoHomeUI())
    val estadoUI: StateFlow<EstadoHomeUI> = _estadoUI.asStateFlow()
    
    init {
        iniciarMonitoreo()
    }
    
    private fun iniciarMonitoreo() {
        viewModelScope.launch {
            while (true) {
                try {
                    _estadoUI.value = _estadoUI.value.copy(cargando = true, error = null)
                    
                    // Simular lectura de sensores
                    val nuevaLectura = simularLecturaSensor()
                    
                    _estadoUI.value = _estadoUI.value.copy(
                        lecturaSensor = nuevaLectura,
                        cargando = false
                    )
                    
                    // Actualizar cada 3 segundos
                    delay(3_000)
                } catch (e: Exception) {
                    _estadoUI.value = _estadoUI.value.copy(
                        cargando = false,
                        error = "Error al leer sensores: ${e.message}"
                    )
                    delay(5_000) // Esperar más tiempo en caso de error
                }
            }
        }
    }
    
    private fun simularLecturaSensor(): LecturaSensor {
        // Simula valores realistas para un vivero
        return LecturaSensor(
            humedadPorcentaje = (45..85).random(),
            temperaturaCelsius = (20..30).random().toFloat()
        )
    }
    
    fun actualizarLectura() {
        viewModelScope.launch {
            try {
                _estadoUI.value = _estadoUI.value.copy(cargando = true, error = null)
                val nuevaLectura = simularLecturaSensor()
                _estadoUI.value = _estadoUI.value.copy(
                    lecturaSensor = nuevaLectura,
                    cargando = false
                )
            } catch (e: Exception) {
                _estadoUI.value = _estadoUI.value.copy(
                    cargando = false,
                    error = "Error al actualizar: ${e.message}"
                )
            }
        }
    }
}


