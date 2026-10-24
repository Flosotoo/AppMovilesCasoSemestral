package com.example.teqmed.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//Catálogo de Roles RBAC
@Entity(tableName = "roles")
data class RolEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String, // TECNICO, JEFE, GERENCIA, ADMIN
    val nombre: String
)

//Catálogo de Permisos del sistema.
@Entity(tableName = "permisos")
data class PermisoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val codigo: String, // REGISTRAR_TRASLADO, VER_JORNADA, etc.
    val descripcion: String
)

//Tabla intermedia N:M de Rol y Permiso.
@Entity(
    tableName = "rol_permiso",
    primaryKeys = ["rol_id", "permiso_id"],
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id"],
            childColumns = ["rol_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PermisoEntity::class,
            parentColumns = ["id"],
            childColumns = ["permiso_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["rol_id"]), Index(value = ["permiso_id"])]
)
data class RolPermisoEntity(
    @ColumnInfo(name = "rol_id")
    val rolId: Long,
    @ColumnInfo(name = "permiso_id")
    val permisoId: Long
)

//Cuentas de usuario locales.
@Entity(
    tableName = "usuarios",
    foreignKeys = [
        ForeignKey(
            entity = RolEntity::class,
            parentColumns = ["id"],
            childColumns = ["rol_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["rut"], unique = true),
        Index(value = ["email"], unique = true),
        Index(value = ["rol_id"])
    ]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rut: String,
    val nombre: String,
    val email: String,
    @ColumnInfo(name = "hash_clave")
    val hashClave: String,
    @ColumnInfo(name = "rol_id")
    val rolId: Long,
    val activo: Boolean = true,
    @ColumnInfo(name = "primer_acceso")
    val primerAcceso: Boolean = true,
    @ColumnInfo(name = "creado_por")
    val creadoPor: Long? = null,
    @ColumnInfo(name = "consentimiento_ubicacion")
    val consentimientoUbicacion: Boolean = false,
    @ColumnInfo(name = "actualizado_en")
    val actualizadoEn: Long = System.currentTimeMillis()
)

//Flota mixta de vehículos.
@Entity(tableName = "vehiculos")
data class VehiculoEntity(
    @PrimaryKey
    val patente: String,
    val marca: String,
    val modelo: String,
    val propiedad: String, // EMPRESA, PARTICULAR
    @ColumnInfo(name = "tipo_combustible")
    val tipoCombustible: String, // BENZINA_93, BENZINA_95, DIESEL
    @ColumnInfo(name = "rendimiento_km_l")
    val rendimientoKmL: Double, // km/L
    val activo: Boolean = true,
    @ColumnInfo(name = "actualizado_en")
    val actualizadoEn: Long = System.currentTimeMillis()
)

//Recintos de salud con distancias oficiales tabuladas desde Concepción.
@Entity(tableName = "destinos")
data class DestinoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "nombre_institucion")
    val nombreInstitucion: String,
    @ColumnInfo(name = "nombre_cliente")
    val nombreCliente: String,
    @ColumnInfo(name = "tipo_recinto")
    val tipoRecinto: String, // CLINICA, HOSPITAL, DIALISIS
    val comuna: String,
    @ColumnInfo(name = "distancia_oficial_km")
    val distanciaOficialKm: Double,
    @ColumnInfo(name = "monto_peaje_estandar")
    val montoPeajeEstandar: Int = 0,
    val activo: Boolean = true,
    @ColumnInfo(name = "actualizado_en")
    val actualizadoEn: Long = System.currentTimeMillis()
)

//Parámetros tarifarios vigentes.
@Entity(tableName = "parametros_sistema")
data class ParametroSistemaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val tipo: String, // PRECIO_COMBUSTIBLE_LITRO_CNE, TARIFA_REEMBOLSO_KM_PARTICULAR, UMBRAL_DESVIACION_PCT
    val valor: Double,
    @ColumnInfo(name = "fuente_referencia")
    val fuenteReferencia: String = "",
    @ColumnInfo(name = "fecha_actualizacion")
    val fechaActualizacion: Long = System.currentTimeMillis()
)

//Registro central de traslados en terreno.
//Congela tarifas aplicadas y soporta operación offline 8h.
@Entity(
    tableName = "traslados",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["tecnico_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = VehiculoEntity::class,
            parentColumns = ["patente"],
            childColumns = ["vehiculo_patente"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = DestinoEntity::class,
            parentColumns = ["id"],
            childColumns = ["destino_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["tecnico_id"]),
        Index(value = ["vehiculo_patente"]),
        Index(value = ["destino_id"]),
        Index(value = ["estado"]),
        Index(value = ["estado_sincronizacion"])
    ]
)
data class TrasladoEntity(
    @PrimaryKey
    val id: String, // UUID
    @ColumnInfo(name = "tecnico_id")
    val tecnicoId: Long,
    @ColumnInfo(name = "vehiculo_patente")
    val vehiculoPatente: String,
    @ColumnInfo(name = "destino_id")
    val destinoId: Long,
    @ColumnInfo(name = "sentido", defaultValue = "IDA")
    val sentido: String = "IDA",

    @ColumnInfo(name = "timestamp_inicio")
    val timestampInicio: Long,
    @ColumnInfo(name = "timestamp_fin")
    val timestampFin: Long? = null,

    @ColumnInfo(name = "odometro_inicial")
    val odometroInicial: Double,
    @ColumnInfo(name = "odometro_final")
    val odometroFinal: Double? = null,

    // Sello GPS puntual 
    @ColumnInfo(name = "latitud_inicio")
    val latitudInicio: Double? = null,
    @ColumnInfo(name = "longitud_inicio")
    val longitudInicio: Double? = null,
    @ColumnInfo(name = "timestamp_gps_inicio")
    val timestampGpsInicio: Long? = null,

    @ColumnInfo(name = "latitud_fin")
    val latitudFin: Double? = null,
    @ColumnInfo(name = "longitud_fin")
    val longitudFin: Double? = null,
    @ColumnInfo(name = "timestamp_gps_fin")
    val timestampGpsFin: Long? = null,

    // Peajes y respaldo fotográfico
    @ColumnInfo(name = "monto_peajes")
    val montoPeajes: Int = 0,
    @ColumnInfo(name = "foto_peaje_uri")
    val fotoPeajeUri: String? = null,
    @ColumnInfo(name = "timestamp_foto_peaje")
    val timestampFotoPeaje: Long? = null,

    // Valorización y congelamiento histórico
    @ColumnInfo(name = "costo_total")
    val costoTotal: Double? = null,
    @ColumnInfo(name = "precio_litro_aplicado")
    val precioLitroAplicado: Double? = null,
    @ColumnInfo(name = "tarifa_km_aplicada")
    val tarifaKmAplicada: Double? = null,

    // Auditoría de desviaciones
    @ColumnInfo(name = "porcentaje_desviacion")
    val porcentajeDesviacion: Double? = null,
    @ColumnInfo(name = "desviacion_detectada")
    val desviacionDetectada: Boolean = false,

    // Estados
    val estado: String = "EN_CURSO", // EN_CURSO, FINALIZADO, VALIDADO, OBSERVADO, RECHAZADO
    @ColumnInfo(name = "estado_sincronizacion")
    val estadoSincronizacion: String = "PENDIENTE_OFFLINE", // PENDIENTE_OFFLINE, SINCRONIZADO

    // Resolución de supervisión Jefatura
    @ColumnInfo(name = "jefe_resolucion_id")
    val jefeResolucionId: Long? = null,
    @ColumnInfo(name = "decision_resolucion")
    val decisionResolucion: String? = null, // VALIDADO, OBSERVADO, RECHAZADO
    @ColumnInfo(name = "comentario_resolucion")
    val comentarioResolucion: String? = null,
    @ColumnInfo(name = "timestamp_resolucion")
    val timestampResolucion: Long? = null
)

// Comprobantes de peaje asociados a un traslado (1:N relacional con CASCADE).
@Entity(
    tableName = "peajes",
    foreignKeys = [
        ForeignKey(
            entity = TrasladoEntity::class,
            parentColumns = ["id"],
            childColumns = ["traslado_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["traslado_id"])]
)
data class PeajeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "traslado_id")
    val trasladoId: String,
    val monto: Int,
    @ColumnInfo(name = "foto_uri")
    val fotoUri: String,
    @ColumnInfo(name = "timestamp_foto")
    val timestampFoto: Long = System.currentTimeMillis()
)

