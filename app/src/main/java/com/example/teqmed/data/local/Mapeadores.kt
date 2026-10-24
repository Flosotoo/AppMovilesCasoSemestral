package com.example.teqmed.data.local

import com.example.teqmed.domain.model.*

//Funciones de mapeo bidireccional entre entidades de Room y modelos de Dominio.

object Mapeadores {

    fun toDomain(entity: RolEntity, permisos: Set<CodigoPermiso> = emptySet()): Rol =
        Rol(
            id = entity.id,
            codigo = try { CodigoRol.valueOf(entity.codigo) } catch (_: Exception) { CodigoRol.TECNICO },
            nombre = entity.nombre,
            permisos = permisos
        )

    fun toDomain(entity: UsuarioEntity, rol: Rol): Usuario =
        Usuario(
            id = entity.id,
            rut = entity.rut,
            nombre = entity.nombre,
            email = entity.email,
            rol = rol,
            activo = entity.activo,
            primerAcceso = entity.primerAcceso,
            creadoPor = entity.creadoPor,
            consentimientoUbicacion = entity.consentimientoUbicacion
        )

    fun toEntity(domain: Usuario, hashClave: String): UsuarioEntity =
        UsuarioEntity(
            id = domain.id,
            rut = domain.rut,
            nombre = domain.nombre,
            email = domain.email,
            hashClave = hashClave,
            rolId = domain.rol.id,
            activo = domain.activo,
            primerAcceso = domain.primerAcceso,
            creadoPor = domain.creadoPor,
            consentimientoUbicacion = domain.consentimientoUbicacion
        )

    fun toDomain(entity: VehiculoEntity): Vehiculo =
        Vehiculo(
            patente = entity.patente,
            marca = entity.marca,
            modelo = entity.modelo,
            propiedad = try { PropiedadVehiculo.valueOf(entity.propiedad) } catch (_: Exception) { PropiedadVehiculo.EMPRESA },
            tipoCombustible = try { TipoCombustible.valueOf(entity.tipoCombustible) } catch (_: Exception) { TipoCombustible.GASOLINA_95 },
            rendimientoKmL = entity.rendimientoKmL,
            activo = entity.activo,
            actualizadoEn = entity.actualizadoEn
        )

    fun toEntity(domain: Vehiculo): VehiculoEntity =
        VehiculoEntity(
            patente = domain.patente,
            marca = domain.marca,
            modelo = domain.modelo,
            propiedad = domain.propiedad.name,
            tipoCombustible = domain.tipoCombustible.name,
            rendimientoKmL = domain.rendimientoKmL,
            activo = domain.activo,
            actualizadoEn = domain.actualizadoEn
        )

    fun toDomain(entity: DestinoEntity): Destino =
        Destino(
            id = entity.id,
            nombreInstitucion = entity.nombreInstitucion,
            nombreCliente = entity.nombreCliente,
            tipoRecinto = try { TipoCliente.valueOf(entity.tipoRecinto) } catch (_: Exception) { TipoCliente.HOSPITAL },
            comuna = entity.comuna,
            distanciaOficialKm = entity.distanciaOficialKm,
            montoPeajeEstandar = entity.montoPeajeEstandar,
            activo = entity.activo,
            actualizadoEn = entity.actualizadoEn
        )

    fun toEntity(domain: Destino): DestinoEntity =
        DestinoEntity(
            id = domain.id,
            nombreInstitucion = domain.nombreInstitucion,
            nombreCliente = domain.nombreCliente,
            tipoRecinto = domain.tipoRecinto.name,
            comuna = domain.comuna,
            distanciaOficialKm = domain.distanciaOficialKm,
            montoPeajeEstandar = domain.montoPeajeEstandar,
            activo = domain.activo,
            actualizadoEn = domain.actualizadoEn
        )

    fun toDomain(entity: ParametroSistemaEntity): ParametroSistema =
        ParametroSistema(
            id = entity.id,
            tipo = try { TipoParametro.valueOf(entity.tipo) } catch (_: Exception) { TipoParametro.PRECIO_COMBUSTIBLE_LITRO_CNE },
            valor = entity.valor,
            fuenteReferencia = entity.fuenteReferencia,
            fechaActualizacion = entity.fechaActualizacion
        )

    fun toEntity(domain: ParametroSistema): ParametroSistemaEntity =
        ParametroSistemaEntity(
            id = domain.id,
            tipo = domain.tipo.name,
            valor = domain.valor,
            fuenteReferencia = domain.fuenteReferencia,
            fechaActualizacion = domain.fechaActualizacion
        )

    fun toDomain(entity: TrasladoEntity): Traslado {
        val ubiInicio = if (entity.latitudInicio != null && entity.longitudInicio != null) {
            UbicacionPuntual(
                latitud = entity.latitudInicio,
                longitud = entity.longitudInicio,
                timestampCaptura = entity.timestampGpsInicio ?: entity.timestampInicio,
                momento = MomentoCaptura.INICIO
            )
        } else null

        val ubiFin = if (entity.latitudFin != null && entity.longitudFin != null && entity.timestampFin != null) {
            UbicacionPuntual(
                latitud = entity.latitudFin,
                longitud = entity.longitudFin,
                timestampCaptura = entity.timestampGpsFin ?: entity.timestampFin,
                momento = MomentoCaptura.CIERRE
            )
        } else null

        val evidencia = entity.fotoPeajeUri?.let { uri ->
            EvidenciaPeaje(
                fotoUriLocal = uri,
                timestampCaptura = entity.timestampFotoPeaje ?: entity.timestampFin ?: entity.timestampInicio
            )
        }

        val resolucion = if (entity.jefeResolucionId != null && entity.decisionResolucion != null) {
            ResolucionJefatura(
                jefeId = entity.jefeResolucionId,
                decision = try { DecisionValidacion.valueOf(entity.decisionResolucion) } catch (_: Exception) { DecisionValidacion.VALIDADO },
                comentario = entity.comentarioResolucion.orEmpty(),
                timestampResolucion = entity.timestampResolucion ?: System.currentTimeMillis()
            )
        } else null

        return Traslado(
            id = entity.id,
            tecnicoId = entity.tecnicoId,
            vehiculoPatente = entity.vehiculoPatente,
            destinoId = entity.destinoId,
            sentido = try { SentidoTraslado.valueOf(entity.sentido) } catch (_: Exception) { SentidoTraslado.IDA },
            timestampInicio = entity.timestampInicio,
            timestampFin = entity.timestampFin,
            odometroInicial = entity.odometroInicial,
            odometroFinal = entity.odometroFinal,
            ubicacionInicio = ubiInicio,
            ubicacionFin = ubiFin,
            montoPeajes = entity.montoPeajes,
            evidenciaPeaje = evidencia,
            costoTotal = entity.costoTotal,
            precioLitroAplicado = entity.precioLitroAplicado,
            tarifaKmAplicada = entity.tarifaKmAplicada,
            porcentajeDesviacion = entity.porcentajeDesviacion,
            desviacionDetectada = entity.desviacionDetectada,
            estado = try { EstadoTraslado.valueOf(entity.estado) } catch (_: Exception) { EstadoTraslado.EN_CURSO },
            estadoSincronizacion = try { EstadoSincronizacion.valueOf(entity.estadoSincronizacion) } catch (_: Exception) { EstadoSincronizacion.PENDIENTE_OFFLINE },
            resolucion = resolucion
        )
    }

    fun toEntity(domain: Traslado): TrasladoEntity =
        TrasladoEntity(
            id = domain.id,
            tecnicoId = domain.tecnicoId,
            vehiculoPatente = domain.vehiculoPatente,
            destinoId = domain.destinoId,
            sentido = domain.sentido.name,
            timestampInicio = domain.timestampInicio,
            timestampFin = domain.timestampFin,
            odometroInicial = domain.odometroInicial,
            odometroFinal = domain.odometroFinal,
            latitudInicio = domain.ubicacionInicio?.latitud,
            longitudInicio = domain.ubicacionInicio?.longitud,
            timestampGpsInicio = domain.ubicacionInicio?.timestampCaptura,
            latitudFin = domain.ubicacionFin?.latitud,
            longitudFin = domain.ubicacionFin?.longitud,
            timestampGpsFin = domain.ubicacionFin?.timestampCaptura,
            montoPeajes = domain.montoPeajes,
            fotoPeajeUri = domain.evidenciaPeaje?.fotoUriLocal,
            timestampFotoPeaje = domain.evidenciaPeaje?.timestampCaptura,
            costoTotal = domain.costoTotal,
            precioLitroAplicado = domain.precioLitroAplicado,
            tarifaKmAplicada = domain.tarifaKmAplicada,
            porcentajeDesviacion = domain.porcentajeDesviacion,
            desviacionDetectada = domain.desviacionDetectada,
            estado = domain.estado.name,
            estadoSincronizacion = domain.estadoSincronizacion.name,
            jefeResolucionId = domain.resolucion?.jefeId,
            decisionResolucion = domain.resolucion?.decision?.name,
            comentarioResolucion = domain.resolucion?.comentario,
            timestampResolucion = domain.resolucion?.timestampResolucion
        )
}
