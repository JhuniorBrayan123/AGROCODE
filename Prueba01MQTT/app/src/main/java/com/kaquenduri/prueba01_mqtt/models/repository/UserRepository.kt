package com.kaquenduri.prueba01_mqtt.models.repository

import com.kaquenduri.prueba01_mqtt.models.dao.UsuarioDao
import com.kaquenduri.prueba01_mqtt.models.entities.Usuario
import kotlinx.coroutines.flow.Flow

class UserRepository(private val usuarioDao: UsuarioDao) {
    
    suspend fun registrarUsuario(usuario: Usuario): Result<Long> {
        return try {
            // Verificar si el correo ya existe
            val usuarioExistente = usuarioDao.getUsuarioPorCorreo(usuario.correo)
            if (usuarioExistente != null) {
                Result.failure(Exception("El correo ya está registrado"))
            } else {
                val id = usuarioDao.insertarUsuario(usuario)
                Result.success(id)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(correo: String, contrasena: String): Result<Usuario> {
        return try {
            val usuario = usuarioDao.login(correo, contrasena)
            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Credenciales incorrectas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun obtenerTodosUsuarios(): Flow<List<Usuario>> {
        return usuarioDao.obtenerTodosUsuarios()
    }
    
    suspend fun eliminarUsuario(usuario: Usuario) {
        usuarioDao.eliminarUsuario(usuario)
    }
}
