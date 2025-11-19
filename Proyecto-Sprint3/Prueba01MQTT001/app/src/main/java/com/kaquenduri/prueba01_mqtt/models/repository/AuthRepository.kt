package com.kaquenduri.prueba01_mqtt.models.repository

import com.google.firebase.auth.FirebaseAuth
import com.kaquenduri.prueba01_mqtt.models.entities.Usuario
import kotlinx.coroutines.tasks.await

class AuthRepository(private val userRepository: UserRepository) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 🔹 Registro con Firebase + guardado local
    suspend fun registrarUsuarioFirebase(usuario: Usuario): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(usuario.correo, usuario.contrasena).await()

            // Guardar también en la base de datos local
            userRepository.registrarUsuario(usuario)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Login con Firebase
    suspend fun loginFirebase(correo: String, contrasena: String): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(correo, contrasena).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 🔹 Cerrar sesión
    fun logout() {
        auth.signOut()
    }

    // 🔹 Obtener usuario actual
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }
}
