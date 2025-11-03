package com.kaquenduri.prueba01_mqtt.ViewModels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.compose.ui.graphics.Color

class ConfiguracionVisualViewModel : ViewModel() {
    
    // Colores predefinidos
    private val coloresPrimarios = listOf(
        Color(0xFF2E7D32), // Verde original
        Color(0xFF00695C)  // Cyan oscuro tirando a verde
    )
    
    private val coloresSecundarios = listOf(
        Color(0xFFFFB278), // Naranja original
        Color(0xFF8BC34A)  // Amarillo tirando a verde
    )
    
    // Estados para colores personalizados
    private val _colorPrimarioSeleccionado = mutableStateOf(0)
    val colorPrimarioSeleccionado: State<Int> = _colorPrimarioSeleccionado
    
    private val _colorSecundarioSeleccionado = mutableStateOf(0)
    val colorSecundarioSeleccionado: State<Int> = _colorSecundarioSeleccionado
    
    // Estado para tamaño de texto global
    private val _tamañoTextoGlobal = mutableStateOf(16f)
    val tamañoTextoGlobal: State<Float> = _tamañoTextoGlobal
    
    // Estados guardados (configuración actual aplicada)
    private val _colorPrimarioGuardado = mutableStateOf(Color(0xFF2E7D32))
    val colorPrimarioGuardado: State<Color> = _colorPrimarioGuardado
    
    private val _colorSecundarioGuardado = mutableStateOf(Color(0xFFFFB278))
    val colorSecundarioGuardado: State<Color> = _colorSecundarioGuardado
    
    private val _tamañoTextoGuardado = mutableStateOf(16f)
    val tamañoTextoGuardado: State<Float> = _tamañoTextoGuardado
    
    fun getColorPrimarioActual(): Color = coloresPrimarios[_colorPrimarioSeleccionado.value]
    fun getColorSecundarioActual(): Color = coloresSecundarios[_colorSecundarioSeleccionado.value]
    
    fun actualizarColorPrimario(indice: Int) {
        _colorPrimarioSeleccionado.value = indice
    }
    
    fun actualizarColorSecundario(indice: Int) {
        _colorSecundarioSeleccionado.value = indice
    }
    
    fun actualizarTamañoTexto(tamaño: Float) {
        _tamañoTextoGlobal.value = tamaño
    }
    
    fun guardarConfiguracion() {
        _colorPrimarioGuardado.value = getColorPrimarioActual()
        _colorSecundarioGuardado.value = getColorSecundarioActual()
        _tamañoTextoGuardado.value = _tamañoTextoGlobal.value
    }
    
    fun resetearConfiguracion() {
        _colorPrimarioSeleccionado.value = 0
        _colorSecundarioSeleccionado.value = 0
        _tamañoTextoGlobal.value = 16f
    }
    
    fun resetearAConfiguracionGuardada() {
        _colorPrimarioSeleccionado.value = coloresPrimarios.indexOf(_colorPrimarioGuardado.value)
        _colorSecundarioSeleccionado.value = coloresSecundarios.indexOf(_colorSecundarioGuardado.value)
        _tamañoTextoGlobal.value = _tamañoTextoGuardado.value
    }
}
