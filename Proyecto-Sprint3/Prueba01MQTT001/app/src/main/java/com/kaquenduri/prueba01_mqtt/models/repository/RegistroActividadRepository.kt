// models/repository/RegistroActividadRepository.kt
package com.kaquenduri.prueba01_mqtt.models.repository

import com.kaquenduri.prueba01_mqtt.models.dao.RegistroActividadDao
import com.kaquenduri.prueba01_mqtt.models.entities.RegistroActividad
import kotlinx.coroutines.flow.Flow

class RegistroActividadRepository(private val registroActividadDao: RegistroActividadDao) {

    suspend fun insertarActividad(actividad: RegistroActividad): Long {
        return registroActividadDao.insertarActividad(actividad)
    }

    fun obtenerActividadesPorCultivo(cultivoId: Int): Flow<List<RegistroActividad>> {
        return registroActividadDao.obtenerActividadesPorCultivo(cultivoId)
    }

    suspend fun actualizarActividad(actividad: RegistroActividad) {
        registroActividadDao.actualizarActividad(actividad)
    }

    suspend fun eliminarActividadesPorCultivo(cultivoId: Int) {
        registroActividadDao.eliminarActividadesPorCultivo(cultivoId)
    }
}