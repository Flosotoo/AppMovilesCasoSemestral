package com.example.teqmed.domain.model

import com.example.teqmed.domain.error.ErrorDominio
import java.time.LocalDateTime

enum class MomentoCaptura { INICIO, CIERRE }

data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val momento: MomentoCaptura,
    val capturadaEn: LocalDateTime = LocalDateTime.now()
)

/**
 * Inmutable: los métodos no modifican el objeto, devuelven uno nuevo con copy().
 * Es lo que después hace que StateFlow funcione sin sorpresas.
 */
data class Traslado(
    val id: Long = 0,
    val codigo: String,
    val tecnicoId: Long,
    val vehiculoId: Long,
    val destinoId: Long,
    val fechaInicio: LocalDateTime,
    val odometroInicial: Double,
    val fechaCierre: LocalDateTime? = null,
    val odometroFinal: Double? = null,
    val estado: EstadoTraslado = EstadoTraslado.EN_CURSO,
    val ubicacionInicio: Ubicacion? = null,
    val costoTotal: Double? = null,
    val sincronizado: Boolean = false,
    val resolucion: Resolucion? = null
) {

    /** null mientras el traslado no se cierra. */
    val distanciaDeclarada: Double?
        get() = odometroFinal?.let { it - odometroInicial }

    val estaEnCurso: Boolean get() = estado == EstadoTraslado.EN_CURSO

    fun cerrar(
        odometroFinal: Double,
        fecha: LocalDateTime = LocalDateTime.now()
    ): Result<Traslado> {
        if (!estaEnCurso)
            return Result.failure(ErrorDominio.EstadoNoPermite(estado, "cerrar"))
        if (odometroFinal <= odometroInicial)
            return Result.failure(ErrorDominio.OdometroInvalido(odometroInicial, odometroFinal))

        return Result.success(
            copy(
                odometroFinal = odometroFinal,
                fechaCierre = fecha,
                estado = EstadoTraslado.FINALIZADO
            )
        )
    }

    fun resolver(r: Resolucion): Result<Traslado> {
        if (estado != EstadoTraslado.FINALIZADO)
            return Result.failure(ErrorDominio.EstadoNoPermite(estado, "resolver"))
        if (r.exigeComentario && r.comentario.isBlank())
            return Result.failure(ErrorDominio.ComentarioRequerido)

        val nuevoEstado = when (r.decision) {
            DecisionValidacion.VALIDADO  -> EstadoTraslado.VALIDADO
            DecisionValidacion.OBSERVADO -> EstadoTraslado.OBSERVADO
            DecisionValidacion.RECHAZADO -> EstadoTraslado.RECHAZADO
        }
        return Result.success(copy(estado = nuevoEstado, resolucion = r))
    }

    fun marcarSincronizado(): Traslado = copy(sincronizado = true)
}