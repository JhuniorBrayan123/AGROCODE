package com.kaquenduri.prueba01_mqtt.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import com.kaquenduri.prueba01_mqtt.models.repository.CultivoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CultivoViewModel : ViewModel() {

    private lateinit var cultivoRepository: CultivoRepository

    private val _uiState = MutableStateFlow<CultivoUiState>(CultivoUiState.Idle)
    val uiState: StateFlow<CultivoUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun initRepository(context: Context) {
        val db = AppDatabase.getDatabase(context)
        cultivoRepository = CultivoRepository(db.cultivoDao())
    }

    fun obtenerCultivosPorUsuario(idUsuario: Int): Flow<List<Cultivo>> {
        return cultivoRepository.obtenerCultivosPorUsuario(idUsuario)
    }

    fun insertarCultivo(cultivo: Cultivo) {
        viewModelScope.launch {
            _uiState.value = CultivoUiState.Loading
            _errorMessage.value = null

            cultivoRepository.insertarCultivo(cultivo)
                .onSuccess {
                    _uiState.value = CultivoUiState.Success("Cultivo creado exitosamente")
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al crear cultivo"
                    _uiState.value = CultivoUiState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun actualizarCultivo(cultivo: Cultivo) {
        viewModelScope.launch {
            _uiState.value = CultivoUiState.Loading
            _errorMessage.value = null

            cultivoRepository.actualizarCultivo(cultivo)
                .onSuccess {
                    _uiState.value = CultivoUiState.Success("Cultivo actualizado exitosamente")
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al actualizar cultivo"
                    _uiState.value = CultivoUiState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun eliminarCultivo(id: Int) {
        viewModelScope.launch {
            _uiState.value = CultivoUiState.Loading
            _errorMessage.value = null

            cultivoRepository.eliminarCultivoPorId(id)
                .onSuccess {
                    _uiState.value = CultivoUiState.Success("Cultivo eliminado exitosamente")
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Error al eliminar cultivo"
                    _uiState.value = CultivoUiState.Error(exception.message ?: "Error desconocido")
                }
        }
    }

    fun resetUiState() {
        _uiState.value = CultivoUiState.Idle
        _errorMessage.value = null
    }

    sealed class CultivoUiState {
        object Idle : CultivoUiState()
        object Loading : CultivoUiState()
        data class Success(val message: String) : CultivoUiState()
        data class Error(val message: String) : CultivoUiState()
    }
}

