package com.kaquenduri.prueba01_mqtt.models.dao

import androidx.room.*
import com.kaquenduri.prueba01_mqtt.models.entities.RegistroActividad
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: RegistroActividad): Long

    @Query("SELECT * FROM registro_actividades WHERE cultivoId = :cultivoId ORDER BY fecha DESC")
    fun obtenerActividadesPorCultivo(cultivoId: Int): Flow<List<RegistroActividad>>

    @Update
    suspend fun actualizarActividad(actividad: RegistroActividad)

    @Query("DELETE FROM registro_actividades WHERE id = :id")
    suspend fun eliminarActividadPorId(id: Int)

    @Query("DELETE FROM registro_actividades WHERE cultivoId = :cultivoId")
    suspend fun eliminarActividadesPorCultivo(cultivoId: Int)

    @Query("SELECT * FROM registro_actividades WHERE idUsuario = :idUsuario ORDER BY fecha DESC")
    fun obtenerTodasActividadesPorUsuario(idUsuario: Int): Flow<List<RegistroActividad>>

    @Query("SELECT * FROM registro_actividades WHERE id = :id")
    fun obtenerActividadPorId(id: Int): Flow<RegistroActividad?>
}