package com.example.teqmed.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RolDao {
    @Query("SELECT * FROM roles ORDER BY id ASC")
    fun obtenerTodos(): Flow<List<RolEntity>>

    @Query("SELECT * FROM roles WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): RolEntity?

    @Query("SELECT * FROM roles WHERE codigo = :codigo LIMIT 1")
    suspend fun obtenerPorCodigo(codigo: String): RolEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rol: RolEntity): Long

    @Query("SELECT COUNT(*) FROM roles")
    suspend fun contar(): Int
}

@Dao
interface PermisoDao {
    @Query("SELECT * FROM permisos ORDER BY codigo ASC")
    fun obtenerTodos(): Flow<List<PermisoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(permiso: PermisoEntity): Long

    @Query("SELECT COUNT(*) FROM permisos")
    suspend fun contar(): Int
}

@Dao
interface RolPermisoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(rolPermiso: RolPermisoEntity)

    @Query("""
        SELECT p.codigo FROM permisos p
        INNER JOIN rol_permiso rp ON p.id = rp.permiso_id
        WHERE rp.rol_id = :rolId
    """)
    suspend fun obtenerCodigosPermisosDeRol(rolId: Long): List<String>

    @Query("SELECT COUNT(*) FROM rol_permiso")
    suspend fun contar(): Int
}

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email AND hash_clave = :hashClave LIMIT 1")
    suspend fun autenticar(email: String, hashClave: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun obtenerPorEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rut = :rut LIMIT 1")
    suspend fun obtenerPorRut(rut: String): UsuarioEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    @Update
    suspend fun actualizar(usuario: UsuarioEntity)

    @Query("UPDATE usuarios SET activo = :activo, actualizado_en = :actualizadoEn WHERE id = :id")
    suspend fun cambiarEstado(id: Long, activo: Boolean, actualizadoEn: Long = System.currentTimeMillis())

    @Query("UPDATE usuarios SET consentimiento_ubicacion = :consentimiento WHERE id = :id")
    suspend fun actualizarConsentimientoUbicacion(id: Long, consentimiento: Boolean)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}

@Dao
interface VehiculoDao {
    @Query("SELECT * FROM vehiculos ORDER BY marca, modelo ASC")
    fun obtenerTodos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE activo = 1 ORDER BY marca, modelo ASC")
    fun obtenerActivos(): Flow<List<VehiculoEntity>>

    @Query("SELECT * FROM vehiculos WHERE patente = :patente LIMIT 1")
    suspend fun obtenerPorPatente(patente: String): VehiculoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vehiculo: VehiculoEntity)

    @Update
    suspend fun actualizar(vehiculo: VehiculoEntity)

    @Query("UPDATE vehiculos SET activo = :activo, actualizado_en = :actualizadoEn WHERE patente = :patente")
    suspend fun cambiarEstado(patente: String, activo: Boolean, actualizadoEn: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM vehiculos")
    suspend fun contar(): Int
}

@Dao
interface DestinoDao {
    @Query("SELECT * FROM destinos ORDER BY nombre_institucion ASC")
    fun obtenerTodos(): Flow<List<DestinoEntity>>

    @Query("SELECT * FROM destinos WHERE activo = 1 ORDER BY nombre_institucion ASC")
    fun obtenerActivos(): Flow<List<DestinoEntity>>

    @Query("SELECT * FROM destinos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Long): DestinoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(destino: DestinoEntity): Long

    @Update
    suspend fun actualizar(destino: DestinoEntity)

    @Query("UPDATE destinos SET activo = :activo, actualizado_en = :actualizadoEn WHERE id = :id")
    suspend fun cambiarEstado(id: Long, activo: Boolean, actualizadoEn: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM destinos")
    suspend fun contar(): Int
}

@Dao
interface ParametroSistemaDao {
    @Query("SELECT * FROM parametros_sistema ORDER BY id ASC")
    fun obtenerTodos(): Flow<List<ParametroSistemaEntity>>

    @Query("SELECT * FROM parametros_sistema WHERE tipo = :tipo LIMIT 1")
    suspend fun obtenerPorTipo(tipo: String): ParametroSistemaEntity?

    @Query("SELECT * FROM parametros_sistema WHERE tipo = :tipo LIMIT 1")
    fun obtenerPorTipoFlow(tipo: String): Flow<ParametroSistemaEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(parametro: ParametroSistemaEntity): Long

    @Update
    suspend fun actualizar(parametro: ParametroSistemaEntity)

    @Query("UPDATE parametros_sistema SET valor = :valor, fecha_actualizacion = :fecha WHERE tipo = :tipo")
    suspend fun actualizarValorPorTipo(tipo: String, valor: Double, fecha: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM parametros_sistema")
    suspend fun contar(): Int
}

@Dao
interface TrasladoDao {
    @Query("SELECT * FROM traslados ORDER BY timestamp_inicio DESC")
    fun obtenerTodos(): Flow<List<TrasladoEntity>>

    @Query("SELECT * FROM traslados WHERE tecnico_id = :tecnicoId ORDER BY timestamp_inicio DESC")
    fun obtenerPorTecnico(tecnicoId: Long): Flow<List<TrasladoEntity>>

    @Query("SELECT * FROM traslados WHERE tecnico_id = :tecnicoId AND timestamp_inicio >= :timestampInicioDia ORDER BY timestamp_inicio DESC")
    fun obtenerJornadaHoy(tecnicoId: Long, timestampInicioDia: Long): Flow<List<TrasladoEntity>>

    @Query("SELECT * FROM traslados WHERE tecnico_id = :tecnicoId AND estado = 'EN_CURSO' LIMIT 1")
    suspend fun obtenerTrasladoEnCurso(tecnicoId: Long): TrasladoEntity?

    @Query("SELECT * FROM traslados WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): TrasladoEntity?

    @Query("SELECT * FROM traslados WHERE id = :id LIMIT 1")
    fun obtenerPorIdFlow(id: String): Flow<TrasladoEntity?>

    @Query("SELECT * FROM traslados WHERE estado_sincronizacion = 'PENDIENTE_OFFLINE'")
    fun obtenerPendientesSincronizacion(): Flow<List<TrasladoEntity>>

    @Query("SELECT * FROM traslados WHERE estado_sincronizacion = 'PENDIENTE_OFFLINE'")
    suspend fun obtenerListaPendientesSincronizacion(): List<TrasladoEntity>

    @Query("SELECT * FROM traslados WHERE estado = 'FINALIZADO' ORDER BY timestamp_inicio DESC")
    fun obtenerFinalizadosParaBandeja(): Flow<List<TrasladoEntity>>

    @Query("""
        SELECT * FROM traslados 
        WHERE timestamp_inicio >= :desde AND timestamp_inicio <= :hasta AND estado IN ('FINALIZADO', 'VALIDADO', 'OBSERVADO', 'RECHAZADO')
        ORDER BY timestamp_inicio DESC
    """)
    suspend fun obtenerParaReportes(desde: Long, hasta: Long): List<TrasladoEntity>

    @Query("""
        SELECT * FROM traslados 
        WHERE timestamp_inicio >= :desde AND timestamp_inicio <= :hasta AND estado IN ('FINALIZADO', 'VALIDADO', 'OBSERVADO', 'RECHAZADO')
        ORDER BY timestamp_inicio DESC
    """)
    fun observarParaReportes(desde: Long, hasta: Long): Flow<List<TrasladoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(traslado: TrasladoEntity)

    @Update
    suspend fun actualizar(traslado: TrasladoEntity)

    @Query("UPDATE traslados SET estado_sincronizacion = 'SINCRONIZADO' WHERE id = :id")
    suspend fun marcarSincronizado(id: String)

    @Query("SELECT COUNT(*) FROM traslados WHERE estado_sincronizacion = 'PENDIENTE_OFFLINE'")
    fun contarPendientesSincronizacionFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM traslados")
    suspend fun contar(): Int
}

@Dao
interface PeajeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(peaje: PeajeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(peajes: List<PeajeEntity>)

    @Query("SELECT * FROM peajes WHERE traslado_id = :trasladoId ORDER BY id ASC")
    fun observarPorTraslado(trasladoId: String): Flow<List<PeajeEntity>>

    @Query("SELECT * FROM peajes WHERE traslado_id = :trasladoId ORDER BY id ASC")
    suspend fun obtenerPorTraslado(trasladoId: String): List<PeajeEntity>

    @Query("DELETE FROM peajes WHERE traslado_id = :trasladoId")
    suspend fun eliminarPorTraslado(trasladoId: String)

    @Query("SELECT COUNT(*) FROM peajes")
    suspend fun contar(): Int
}

