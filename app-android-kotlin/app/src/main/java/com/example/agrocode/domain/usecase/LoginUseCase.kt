package com.example.agrocode.domain.usecase

import com.example.agrocode.domain.model.CredencialesLogin
import com.example.agrocode.domain.model.Usuario

interface LoginUseCase {
    suspend fun iniciarSesion(credenciales: CredencialesLogin): ResultadoLogin
    data class Error(val mensaje: String) : ResultadoLogin()
}

sealed class ResultadoLogin {
    data class Exitoso(val usuario: Usuario) : ResultadoLogin()
    object Cargando : ResultadoLogin()
}
