package com.example.agrocode.data.repository

import com.example.agrocode.domain.model.CredencialesLogin
import com.example.agrocode.domain.model.Usuario
import com.example.agrocode.domain.usecase.ResultadoLogin

interface UsuarioRepository {
    suspend fun iniciarSesion(credenciales: CredencialesLogin): ResultadoLogin
    suspend fun registrarUsuario(datosRegistro: com.example.agrocode.domain.model.DatosRegistro): ResultadoLogin
}
