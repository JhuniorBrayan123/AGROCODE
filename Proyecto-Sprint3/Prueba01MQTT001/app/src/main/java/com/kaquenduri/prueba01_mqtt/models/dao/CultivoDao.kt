package com.kaquenduri.prueba01_mqtt.models.dao

import androidx.room.*
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import kotlinx.coroutines.flow.Flow

@Dao
interface CultivoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarCultivo(cultivo: Cultivo): Long

    @Query("SELECT * FROM cultivos WHERE idUsuario = :idUsuario")
    fun obtenerTodosCultivosPorUsuario(idUsuario: Int): Flow<List<Cultivo>>

    @Query("SELECT * FROM cultivos")
    fun obtenerTodosCultivos(): Flow<List<Cultivo>>

    @Update
    suspend fun actualizarCultivo(cultivo: Cultivo)

    @Query("DELETE FROM cultivos WHERE id = :id")
    suspend fun eliminarCultivoPorId(id: Int)

    @Query("SELECT * FROM cultivos WHERE id = :id")
    fun obtenerCultivoPorId(id: Int): Flow<Cultivo?>

}
