package com.kaquenduri.prueba01_mqtt.models.repository

import com.kaquenduri.prueba01_mqtt.models.dao.AlertaDao
import com.kaquenduri.prueba01_mqtt.models.entities.Alerta
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val alertaDao: AlertaDao) {
    
    fun obtenerTodasAlertas(): Flow<List<Alerta>> {
        return alertaDao.obtenerTodasAlertas()
    }
    
    suspend fun guardarAlerta(mensaje: String): Result<Long> {
        return try {
            val alerta = Alerta(mensaje = mensaje)
            val id = alertaDao.insertarAlerta(alerta)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun eliminarTodasAlertas() {
        alertaDao.eliminarTodasAlertas()
    }
    
    suspend fun eliminarAlerta(alerta: Alerta) {
        alertaDao.eliminarAlerta(alerta)
    }
    
    suspend fun contarAlertas(): Int {
        return alertaDao.contarAlertas()
    }
}
