package com.kaquenduri.prueba01_mqtt.models.repository

import com.kaquenduri.prueba01_mqtt.models.dao.CultivoDao
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import kotlinx.coroutines.flow.Flow

class CultivoRepository(private val cultivoDao: CultivoDao) {

    // Insertar nuevo cultivo
    suspend fun insertarCultivo(cultivo: Cultivo): Result<Long> {
        return try {
            val id = cultivoDao.insertarCultivo(cultivo)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun obtenerCultivosPorUsuario(idUsuario: Int): Flow<List<Cultivo>> {
        return cultivoDao.obtenerTodosCultivosPorUsuario(idUsuario)
    }

    // Actualizar cultivo existente
    suspend fun actualizarCultivo(cultivo: Cultivo): Result<Unit> {
        return try {
            cultivoDao.actualizarCultivo(cultivo)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Eliminar cultivo por ID
    suspend fun eliminarCultivoPorId(id: Int): Result<Unit> {
        return try {
            cultivoDao.eliminarCultivoPorId(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
