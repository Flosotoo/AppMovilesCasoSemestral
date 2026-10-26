package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/**
 * Validador para rangos de fechas en reportes analíticos de Gerencia (HU30, HU31, RF-26).
 */
object ValidadorPeriodoReporte {

    fun validarRango(desdeMillis: Long, hastaMillis: Long): ValidacionResult {
        if (desdeMillis <= 0L || hastaMillis <= 0L) {
            return ValidacionResult.error("Debe seleccionar ambas fechas del período")
        }
        if (desdeMillis > hastaMillis) {
            return ValidacionResult.error("La fecha de inicio no puede ser posterior a la fecha de término")
        }
        return ValidacionResult.ok()
    }
}
