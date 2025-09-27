package com.example.agrocode.presentation.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrocode.data.repository.UsuarioRepositoryImpl
import com.example.agrocode.domain.model.DatosRegistro
import com.example.agrocode.domain.usecase.ResultadoLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegistroScreenViewModel : ViewModel() {
    
    private val repositorioUsuario = UsuarioRepositoryImpl()
    
    private val _estadoUI = MutableStateFlow(EstadoRegistroUI())
    val estadoUI: StateFlow<EstadoRegistroUI> = _estadoUI.asStateFlow()
    
    fun actualizarNombre(nombre: String) {
        _estadoUI.value = _estadoUI.value.copy(nombre = nombre)
    }
    
    fun actualizarApellido(apellido: String) {
        _estadoUI.value = _estadoUI.value.copy(apellido = apellido)
    }
    
    fun actualizarCorreo(correo: String) {
        _estadoUI.value = _estadoUI.value.copy(correo = correo)
    }
    
    fun actualizarTelefono(telefono: String) {
        _estadoUI.value = _estadoUI.value.copy(telefono = telefono)
    }
    
    fun actualizarContrasena(contrasena: String) {
        _estadoUI.value = _estadoUI.value.copy(contrasena = contrasena)
    }
    
    fun actualizarConfirmarContrasena(confirmarContrasena: String) {
        _estadoUI.value = _estadoUI.value.copy(confirmarContrasena = confirmarContrasena)
    }
    
    fun crearCuenta() {
        val datosRegistro = DatosRegistro(
            correo = _estadoUI.value.correo,
            contrasena = _estadoUI.value.contrasena,
            confirmarContrasena = _estadoUI.value.confirmarContrasena,
            nombre = _estadoUI.value.nombre,
            apellido = _estadoUI.value.apellido,
            telefono = _estadoUI.value.telefono
        )
        
        viewModelScope.launch {
            _estadoUI.value = _estadoUI.value.copy(cargando = true, mensajeError = null)
            
            when (val resultado = repositorioUsuario.registrarUsuario(datosRegistro)) {
                is ResultadoLogin.Exitoso -> {
                    _estadoUI.value = _estadoUI.value.copy(
                        cargando = false,
                        registroExitoso = true,
                        usuario = resultado.usuario
                    )
                }
                is ResultadoLogin.Error -> {
                    _estadoUI.value = _estadoUI.value.copy(
                        cargando = false,
                        mensajeError = resultado.mensaje
                    )
                }
                is ResultadoLogin.Cargando -> {
                    _estadoUI.value = _estadoUI.value.copy(cargando = true)
                }
            }
        }
    }
    
    fun limpiarError() {
        _estadoUI.value = _estadoUI.value.copy(mensajeError = null)
    }
}

data class EstadoRegistroUI(
    val nombre: String = "",
    val apellido: String = "",
    val correo: String = "",
    val telefono: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val registroExitoso: Boolean = false,
    val usuario: com.example.agrocode.domain.model.Usuario? = null
)
