package com.kaquenduri.prueba01_mqtt.models.dao

import androidx.room.*
import com.kaquenduri.prueba01_mqtt.models.entities.Alerta
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertaDao {
    
    @Query("SELECT * FROM alertas ORDER BY fechaHora DESC")
    fun obtenerTodasAlertas(): Flow<List<Alerta>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlerta(alerta: Alerta): Long
    
    @Query("DELETE FROM alertas")
    suspend fun eliminarTodasAlertas()
    
    @Delete
    suspend fun eliminarAlerta(alerta: Alerta)
    
    @Query("SELECT COUNT(*) FROM alertas")
    suspend fun contarAlertas(): Int
}
