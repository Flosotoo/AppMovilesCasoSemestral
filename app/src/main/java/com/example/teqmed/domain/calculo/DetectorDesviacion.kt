package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.Desviacion

/**
 * Auditoria bidireccional de distancias (RN03, RF-11).
 * Separado de la calculadora para poder explicarlo y modificarlo
 * de forma aislada durante el Live Coding.
 */
object DetectorDesviacion {

    fun evaluar(
        declarada: Double,
        oficial: Double,
        umbralPorcentaje: Double = Desviacion.UMBRAL_PCT_DEFECTO,
        umbralKmAbsoluto: Double = Desviacion.UMBRAL_KM_ABSOLUTO
    ): Desviacion = Desviacion(
        distanciaDeclarada = declarada,
        distanciaOficial = oficial,
        umbralPorcentaje = umbralPorcentaje,
        umbralKmAbsoluto = umbralKmAbsoluto
    )
}
