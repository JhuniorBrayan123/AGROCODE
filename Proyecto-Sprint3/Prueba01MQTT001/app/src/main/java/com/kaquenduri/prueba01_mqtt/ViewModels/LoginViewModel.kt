package com.kaquenduri.prueba01_mqtt.ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaquenduri.prueba01_mqtt.models.database.AppDatabase
import com.kaquenduri.prueba01_mqtt.models.repository.AuthRepository
import com.kaquenduri.prueba01_mqtt.models.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private lateinit var userRepository: UserRepository
    private lateinit var authRepository: AuthRepository

    // 🔹 Inicializa ambos repositorios (local y Firebase)
    fun initRepository(context: Context) {
        val db = AppDatabase.getDatabase(context)
        userRepository = UserRepository(db.usuarioDao())
        authRepository = AuthRepository(userRepository)
    }

    fun onEmailChanged(value: String) {
        _email.value = value
    }

    fun onPasswordChanged(value: String) {
        _password.value = value
    }

    fun login() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""

            // Validaciones básicas
            if (_email.value.isBlank() || _password.value.isBlank()) {
                _errorMessage.value = "Completa todos los campos."
                _isLoading.value = false
                return@launch
            }

            try {
                val correo = _email.value.trim().lowercase()
                val pass = _password.value

                // 🔹 Primero intenta login con Firebase
                val result = authRepository.loginFirebase(correo, pass)

                result.onSuccess {
                    _loginSuccess.value = true
                }.onFailure {
                    // 🔸 Si falla en Firebase, intenta login local como respaldo
                    val localResult = userRepository.login(correo, pass)
                    localResult.onSuccess {
                        _loginSuccess.value = true
                    }.onFailure { localError ->
                        _errorMessage.value =
                            it.message ?: localError.message ?: "Error al iniciar sesión."
                    }
                }

            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLoginState() {
        _loginSuccess.value = false
    }
}
