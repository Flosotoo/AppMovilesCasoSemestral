package com.example.teqmed.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//Pre-población de datos semilla para la base de datos local SQLite Room.

object DatosIniciales {

    suspend fun sembrar(db: TeqRutaDatabase) = withContext(Dispatchers.IO) {
        // 1. Roles y Permisos (si no existen)
        if (db.rolDao().contar() == 0) {
            val rolAdminId = db.rolDao().insertar(RolEntity(codigo = "ADMIN", nombre = "Administrador del Sistema"))
            val rolJefeId = db.rolDao().insertar(RolEntity(codigo = "JEFE", nombre = "Jefe de Servicio"))
            val rolTecnicoId = db.rolDao().insertar(RolEntity(codigo = "TECNICO", nombre = "Técnico en Terreno"))
            val rolGerenciaId = db.rolDao().insertar(RolEntity(codigo = "GERENCIA", nombre = "Gerencia y Finanzas"))

            val pRegTraslado = db.permisoDao().insertar(PermisoEntity(codigo = com.example.teqmed.domain.model.CodigoPermiso.REGISTRAR_TRASLADO.name, descripcion = "Iniciar y cerrar traslados en terreno"))
            val pValidar = db.permisoDao().insertar(PermisoEntity(codigo = com.example.teqmed.domain.model.CodigoPermiso.VALIDAR_TRASLADO.name, descripcion = "Auditar y resolver traslados"))
            val pCostos = db.permisoDao().insertar(PermisoEntity(codigo = com.example.teqmed.domain.model.CodigoPermiso.VER_COSTOS.name, descripcion = "Consultar reportes de costos"))
            val pMaestros = db.permisoDao().insertar(PermisoEntity(codigo = com.example.teqmed.domain.model.CodigoPermiso.ADMIN_MAESTROS.name, descripcion = "Mantener vehículos, destinos y tarifas"))
            val pUsuarios = db.permisoDao().insertar(PermisoEntity(codigo = com.example.teqmed.domain.model.CodigoPermiso.ADMIN_USUARIOS.name, descripcion = "Crear y desactivar cuentas de usuario"))

            // Asignar permisos a Técnico
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolTecnicoId, permisoId = pRegTraslado))

            // Asignar permisos a Jefe
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolJefeId, permisoId = pValidar))

            // Asignar permisos a Gerencia
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolGerenciaId, permisoId = pCostos))

            // Asignar permisos a Admin
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolAdminId, permisoId = pUsuarios))
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolAdminId, permisoId = pMaestros))
            db.rolPermisoDao().insertar(RolPermisoEntity(rolId = rolAdminId, permisoId = pCostos))

            // 2. Usuarios semilla
            db.usuarioDao().insertar(
                UsuarioEntity(
                    rut = "11111111-1",
                    nombre = "Administrador TEQMED",
                    email = "admin@teqmed.cl",
                    hashClave = "admin123",
                    rolId = rolAdminId,
                    activo = true,
                    primerAcceso = false,
                    consentimientoUbicacion = true
                )
            )
            db.usuarioDao().insertar(
                UsuarioEntity(
                    rut = "22222222-2",
                    nombre = "Carlos Muñoz (Jefe Servicio)",
                    email = "jefe@teqmed.cl",
                    hashClave = "jefe123",
                    rolId = rolJefeId,
                    activo = true,
                    primerAcceso = false,
                    consentimientoUbicacion = true
                )
            )
            db.usuarioDao().insertar(
                UsuarioEntity(
                    rut = "33333333-3",
                    nombre = "Juan Perez (Tecnico Terreno)",
                    email = "tecnico@teqmed.cl",
                    hashClave = "tecnico123",
                    rolId = rolTecnicoId,
                    activo = true,
                    primerAcceso = false,
                    consentimientoUbicacion = true
                )
            )
            db.usuarioDao().insertar(
                UsuarioEntity(
                    rut = "44444444-4",
                    nombre = "Sofia Valenzuela (Gerencia)",
                    email = "gerencia@teqmed.cl",
                    hashClave = "gerencia123",
                    rolId = rolGerenciaId,
                    activo = true,
                    primerAcceso = false,
                    consentimientoUbicacion = true
                )
            )
        }

        // Actualizar claves si la BD fue creada previamente con la semilla abreviada tec123
        val uTec = db.usuarioDao().obtenerPorEmail("tecnico@teqmed.cl")
        if (uTec != null && uTec.hashClave != "tecnico123") {
            db.usuarioDao().actualizar(uTec.copy(hashClave = "tecnico123"))
        }
        val uGer = db.usuarioDao().obtenerPorEmail("gerencia@teqmed.cl")
        if (uGer != null && uGer.hashClave != "gerencia123") {
            db.usuarioDao().actualizar(uGer.copy(hashClave = "gerencia123"))
        }

        // 3. Vehículos de prueba
        if (db.vehiculoDao().contar() == 0) {
            db.vehiculoDao().insertar(
                VehiculoEntity(
                    patente = "KJTR45",
                    marca = "Toyota",
                    modelo = "Hilux 2.4",
                    propiedad = "EMPRESA",
                    tipoCombustible = "DIESEL",
                    rendimientoKmL = 12.0,
                    activo = true
                )
            )
            db.vehiculoDao().insertar(
                VehiculoEntity(
                    patente = "BBDD12",
                    marca = "Hyundai",
                    modelo = "Accent 1.4",
                    propiedad = "PARTICULAR",
                    tipoCombustible = "BENZINA_95",
                    rendimientoKmL = 14.5,
                    activo = true
                )
            )
            db.vehiculoDao().insertar(
                VehiculoEntity(
                    patente = "LLPP89",
                    marca = "Peugeot",
                    modelo = "Partner HDI",
                    propiedad = "EMPRESA",
                    tipoCombustible = "DIESEL",
                    rendimientoKmL = 15.0,
                    activo = true
                )
            )
        }

        // 4. Destinos (Concepción, Talcahuano, Chillán, Los Ángeles, Temuco)
        if (db.destinoDao().contar() == 0) {
            // Gran Concepción
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Hospital Guillermo Grant Benavente",
                    nombreCliente = "Servicio de Salud Concepción",
                    tipoRecinto = "HOSPITAL",
                    comuna = "Concepción",
                    distanciaOficialKm = 5.2,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Centro de Diálisis NefroCare Concepción",
                    nombreCliente = "NefroCare Chile S.A.",
                    tipoRecinto = "DIALISIS",
                    comuna = "Concepción",
                    distanciaOficialKm = 3.8,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Clínica Biobío",
                    nombreCliente = "Empresas Banmédica",
                    tipoRecinto = "CLINICA",
                    comuna = "Talcahuano",
                    distanciaOficialKm = 14.5,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Hospital Las Higueras",
                    nombreCliente = "Servicio de Salud Talcahuano",
                    tipoRecinto = "HOSPITAL",
                    comuna = "Talcahuano",
                    distanciaOficialKm = 18.0,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )

            // Red interurbana habitual del caso
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Hospital Clínico Herminda Martín",
                    nombreCliente = "Servicio de Salud Ñuble",
                    tipoRecinto = "HOSPITAL",
                    comuna = "Chillán",
                    distanciaOficialKm = 98.0,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Complejo Asistencial Dr. Víctor Ríos Ruiz",
                    nombreCliente = "Servicio de Salud Biobío",
                    tipoRecinto = "HOSPITAL",
                    comuna = "Los Ángeles",
                    distanciaOficialKm = 125.0,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
            db.destinoDao().insertar(
                DestinoEntity(
                    nombreInstitucion = "Hospital Dr. Hernán Henríquez Aravena",
                    nombreCliente = "Servicio de Salud Araucanía Sur",
                    tipoRecinto = "HOSPITAL",
                    comuna = "Temuco",
                    distanciaOficialKm = 260.0,
                    montoPeajeEstandar = 0,
                    activo = true
                )
            )
        }

        // 5. Parámetros del sistema pre-sembrados en Room (erradica constantes en código duro)
        if (db.parametroSistemaDao().contar() == 0) {
            db.parametroSistemaDao().insertar(
                ParametroSistemaEntity(
                    tipo = "PRECIO_COMBUSTIBLE_LITRO_CNE",
                    valor = 1240.0, // $/L referencial base CNE (fallback local)
                    fuenteReferencia = "Comisión Nacional de Energía (CNE / Energía Abierta)"
                )
            )
            db.parametroSistemaDao().insertar(
                ParametroSistemaEntity(
                    tipo = "UMBRAL_DESVIACION_PORCENTAJE",
                    valor = 10.0, // 10% de tolerancia bidireccional (RN03)
                    fuenteReferencia = "Norma de Auditoría Operacional TEQMED SpA"
                )
            )
            db.parametroSistemaDao().insertar(
                ParametroSistemaEntity(
                    tipo = "UMBRAL_DESVIACION_KM_ABSOLUTO",
                    valor = 5.0, // 5 km de diferencia absoluta mínima de alerta (RN03)
                    fuenteReferencia = "Norma de Auditoría Operacional TEQMED SpA"
                )
            )
        }
    }
}
