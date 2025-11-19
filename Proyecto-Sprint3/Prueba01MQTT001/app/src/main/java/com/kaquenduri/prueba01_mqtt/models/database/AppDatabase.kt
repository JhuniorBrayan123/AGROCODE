package com.kaquenduri.prueba01_mqtt.models.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.kaquenduri.prueba01_mqtt.models.dao.AlertaDao
import com.kaquenduri.prueba01_mqtt.models.dao.UsuarioDao
import com.kaquenduri.prueba01_mqtt.models.dao.CultivoDao
import com.kaquenduri.prueba01_mqtt.models.dao.RegistroActividadDao
import com.kaquenduri.prueba01_mqtt.models.dao.ChatDao
import com.kaquenduri.prueba01_mqtt.models.entities.Alerta
import com.kaquenduri.prueba01_mqtt.models.entities.Usuario
import com.kaquenduri.prueba01_mqtt.models.entities.Cultivo
import com.kaquenduri.prueba01_mqtt.models.entities.RegistroActividad
import com.kaquenduri.prueba01_mqtt.models.entities.ChatEntry

@Database(
    entities = [
        Usuario::class,
        Alerta::class,
        Cultivo::class,
        RegistroActividad::class,
        ChatEntry::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun cultivoDao(): CultivoDao
    abstract fun registroActividadDao(): RegistroActividadDao //  NUEVO: agregado sin modificar lo demás
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agrocode_database"
                )
                    .fallbackToDestructiveMigration() //  permite reconstruir BD si hay cambios
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}