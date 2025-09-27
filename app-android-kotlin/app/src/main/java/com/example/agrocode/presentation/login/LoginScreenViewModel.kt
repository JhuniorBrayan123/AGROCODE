package com.example.agrocode.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agrocode.data.repository.UsuarioRepositoryImpl
import com.example.agrocode.domain.model.CredencialesLogin
import com.example.agrocode.domain.usecase.ResultadoLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenViewModel : ViewModel() {
    
    private val repositorioUsuario = UsuarioRepositoryImpl()
    
    private val _estadoUI = MutableStateFlow(EstadoLoginUI())
    val estadoUI: StateFlow<EstadoLoginUI> = _estadoUI.asStateFlow()
    
    fun actualizarCorreo(correo: String) {
        _estadoUI.value = _estadoUI.value.copy(correo = correo)
    }
    
    fun actualizarContrasena(contrasena: String) {
        _estadoUI.value = _estadoUI.value.copy(contrasena = contrasena)
    }
    
    fun iniciarSesion() {
        val credenciales = CredencialesLogin(
            correo = _estadoUI.value.correo,
            contrasena = _estadoUI.value.contrasena
        )
        
        viewModelScope.launch {
            _estadoUI.value = _estadoUI.value.copy(cargando = true, mensajeError = null)
            
            when (val resultado = repositorioUsuario.iniciarSesion(credenciales)) {
                is ResultadoLogin.Exitoso -> {
                    _estadoUI.value = _estadoUI.value.copy(
                        cargando = false,
                        loginExitoso = true,
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

data class EstadoLoginUI(
    val correo: String = "",
    val contrasena: String = "",
    val cargando: Boolean = false,
    val mensajeError: String? = null,
    val loginExitoso: Boolean = false,
    val usuario: com.example.agrocode.domain.model.Usuario? = null
)