package com.kaquenduri.prueba01_mqtt.ViewModels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.entities.Usuario
import com.kaquenduri.prueba01_mqtt.models.repository.UserRepository
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var confirm = mutableStateOf("")
    var mensaje = mutableStateOf("")

    private lateinit var userRepository: UserRepository

    fun initRepository(context: Context) {
        val database = AppDatabase.getDatabase(context)
        userRepository = UserRepository(database.usuarioDao())
    }

    fun onEmailChanged(newEmail: String) {
        email.value = newEmail
    }

    fun onPasswordChanged(newPass: String) {
        password.value = newPass
    }

    fun onConfirmChanged(newConfirm: String) {
        confirm.value = newConfirm
    }

    fun registrarUsuario(onSuccess: () -> Unit) {
        if (!::userRepository.isInitialized) {
            mensaje.value = "Error interno: repositorio no inicializado."
            return
        }

        if (email.value.isBlank() || password.value.isBlank() || confirm.value.isBlank()) {
            mensaje.value = "Completa todos los campos."
            return
        }

        if (password.value != confirm.value) {
            mensaje.value = "Las contraseñas no coinciden."
            return
        }

        viewModelScope.launch {
            val usuario = Usuario(
                nombre = "", // Si tu entidad requiere estos campos, puedes pedirlos luego
                apellido = "",
                correo = email.value,
                contrasena = password.value
            )

            val result = userRepository.registrarUsuario(usuario)
            result.onSuccess {
                mensaje.value = "Registro exitoso."
                onSuccess()
            }.onFailure {
                mensaje.value = "Error al registrar: ${it.message}"
            }
        }
    }
}
