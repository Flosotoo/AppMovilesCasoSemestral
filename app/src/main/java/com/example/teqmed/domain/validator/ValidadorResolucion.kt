package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.DecisionValidacion
import com.example.teqmed.domain.model.ValidacionResult

/** Reglas de la bandeja de validacion de jefatura (RF-24, HU28). */
object ValidadorResolucion {

    /** Observar y rechazar exigen comentario; validar no. */
    fun validarComentario(
        decision: DecisionValidacion?,
        comentario: String
    ): ValidacionResult = when {
        decision == null -> ValidacionResult.error("Debe elegir una decision")
        decision == DecisionValidacion.VALIDADO -> ValidacionResult.ok()
        comentario.isBlank() -> ValidacionResult.error("Indique el motivo de la observacion")
        comentario.trim().length < 5 -> ValidacionResult.error("Describa el motivo con mas detalle")
        else -> ValidacionResult.ok()
    }
}