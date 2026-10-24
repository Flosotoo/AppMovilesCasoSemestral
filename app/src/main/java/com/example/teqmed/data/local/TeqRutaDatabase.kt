package com.example.teqmed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//Base de datos SQLite Room de TeqRuta.
//Soporta operación offline.

@Database(
    entities = [
        RolEntity::class,
        PermisoEntity::class,
        RolPermisoEntity::class,
        UsuarioEntity::class,
        VehiculoEntity::class,
        DestinoEntity::class,
        ParametroSistemaEntity::class,
        TrasladoEntity::class,
        PeajeEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Conversores::class)
abstract class TeqRutaDatabase : RoomDatabase() {

    abstract fun rolDao(): RolDao
    abstract fun permisoDao(): PermisoDao
    abstract fun rolPermisoDao(): RolPermisoDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun vehiculoDao(): VehiculoDao
    abstract fun destinoDao(): DestinoDao
    abstract fun parametroSistemaDao(): ParametroSistemaDao
    abstract fun trasladoDao(): TrasladoDao
    abstract fun peajeDao(): PeajeDao

    companion object {
        @Volatile
        private var INSTANCE: TeqRutaDatabase? = null

        fun getDatabase(context: Context): TeqRutaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TeqRutaDatabase::class.java,
                    "teqruta_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                // Garantizar la siembra inicial inmediatamente en background
                CoroutineScope(Dispatchers.IO).launch {
                    DatosIniciales.sembrar(instance)
                }

                instance
            }
        }
    }
}
