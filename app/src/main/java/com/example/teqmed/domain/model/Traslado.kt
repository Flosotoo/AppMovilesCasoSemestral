package com.example.teqmed.domain.model

import java.util.UUID

/**
 * Entidad central del negocio (EP02, EP03, EP04, EP05).
 *
 * Inmutable: los metodos devuelven una copia nueva en vez de mutar.
 * Eso mantiene el StateFlow predecible y permite explicar la maquina de
 * estados en vivo sin efectos colaterales.
 *
 * Las tarifas aplicadas quedan CONGELADAS (RF-17): un traslado de octubre
 * no cambia de monto si en noviembre sube el precio del litro.
 */
data class Traslado(
    val id: String = UUID.randomUUID().toString(),
    val tecnicoId: Long,
    val vehiculoPatente: String,
    val destinoId: Long,

    val timestampInicio: Long,
    val timestampFin: Long? = null,

    val odometroInicial: Double,
    val odometroFinal: Double? = null,

    val ubicacionInicio: UbicacionPuntual? = null,
    val ubicacionFin: UbicacionPuntual? = null,

    val montoPeajes: Int = 0,
    val evidenciaPeaje: EvidenciaPeaje? = null,

    val costoTotal: Double? = null,
    val precioLitroAplicado: Double? = null,
    val tarifaKmAplicada: Double? = null,

    val porcentajeDesviacion: Double? = null,
    val desviacionDetectada: Boolean = false,

    val estado: EstadoTraslado = EstadoTraslado.EN_CURSO,
    val estadoSincronizacion: EstadoSincronizacion = EstadoSincronizacion.PENDIENTE_OFFLINE,

    val resolucion: ResolucionJefatura? = null
) {

    /** D declarada = odometro final - odometro inicial. Null si sigue abierto. */
    fun distanciaDeclarada(): Double? =
        odometroFinal?.let { it - odometroInicial }

    fun estaEnCurso(): Boolean = estado == EstadoTraslado.EN_CURSO
    fun puedeResolverse(): Boolean = estado == EstadoTraslado.FINALIZADO
    fun estaPendienteDeEnvio(): Boolean =
        estadoSincronizacion == EstadoSincronizacion.PENDIENTE_OFFLINE

    /**
     * Check-out. Recibe el resultado ya valorizado por CalculadoraCosto:
     * el traslado no calcula, solo guarda lo que el dominio determino.
     */
    fun cerrar(
        odometroFinal: Double,
        montoPeajes: Int,
        evidencia: EvidenciaPeaje?,
        ubicacionFin: UbicacionPuntual?,
        valorizacion: ResultadoValorizacion,
        timestampFin: Long = System.currentTimeMillis()
    ): Traslado = copy(
        odometroFinal = odometroFinal,
        montoPeajes = montoPeajes,
        evidenciaPeaje = evidencia,
        ubicacionFin = ubicacionFin,
        timestampFin = timestampFin,
        costoTotal = valorizacion.costoTotal,
        precioLitroAplicado = valorizacion.precioLitroAplicado,
        tarifaKmAplicada = valorizacion.tarifaKmAplicada,
        porcentajeDesviacion = valorizacion.desviacion.porcentajeConSigno,
        desviacionDetectada = valorizacion.desviacion.superaUmbral,
        estado = EstadoTraslado.FINALIZADO
    )

    /** Aplica la resolucion de jefatura (RF-24). */
    fun aplicarResolucion(r: ResolucionJefatura): Traslado {
        val nuevoEstado = when (r.decision) {
            DecisionValidacion.VALIDADO -> EstadoTraslado.VALIDADO
            DecisionValidacion.OBSERVADO -> EstadoTraslado.OBSERVADO
            DecisionValidacion.RECHAZADO -> EstadoTraslado.RECHAZADO
        }
        return copy(estado = nuevoEstado, resolucion = r)
    }

    fun marcarSincronizado(): Traslado =
        copy(estadoSincronizacion = EstadoSincronizacion.SINCRONIZADO)
}