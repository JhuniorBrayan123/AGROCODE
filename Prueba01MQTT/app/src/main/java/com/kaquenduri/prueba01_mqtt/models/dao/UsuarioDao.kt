package com.kaquenduri.prueba01_mqtt.models.dao

import androidx.room.*
import com.kaquenduri.prueba01_mqtt.models.entities.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND contrasena = :contrasena")
    suspend fun login(correo: String, contrasena: String): Usuario?
    
    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    suspend fun getUsuarioPorCorreo(correo: String): Usuario?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: Usuario): Long
    
    @Query("SELECT * FROM usuarios")
    fun obtenerTodosUsuarios(): Flow<List<Usuario>>
    
    @Delete
    suspend fun eliminarUsuario(usuario: Usuario)
}   
