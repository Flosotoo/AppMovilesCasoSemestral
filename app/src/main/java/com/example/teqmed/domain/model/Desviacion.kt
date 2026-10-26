package com.example.teqmed.domain.model

import kotlin.math.abs

/**
 * Auditoria bidireccional de distancia (RN03, RF-11).
 *
 * Se marca desviacion si se cumple CUALQUIERA de las dos condiciones:
 *   - El porcentaje absoluto supera el umbral porcentual (por defecto 10%)
 *   - La diferencia absoluta supera el umbral en km (por defecto 5 km)
 *
 * Ambos umbrales son configurables en Room mediante HU36.
 */
data class Desviacion(
    val distanciaDeclarada: Double,
    val distanciaOficial: Double,
    val umbralPorcentaje: Double = UMBRAL_PCT_DEFECTO,
    val umbralKmAbsoluto: Double = UMBRAL_KM_ABSOLUTO
) {
    val diferenciaKm: Double = distanciaDeclarada - distanciaOficial

    val porcentajeConSigno: Double =
        if (distanciaOficial <= 0.0) 0.0
        else diferenciaKm / distanciaOficial * 100.0

    val porcentajeAbsoluto: Double = abs(porcentajeConSigno)

    val superaUmbral: Boolean =
        porcentajeAbsoluto > umbralPorcentaje || abs(diferenciaKm) > umbralKmAbsoluto

    val esPorExceso: Boolean = superaUmbral && diferenciaKm > 0
    val esPorDefecto: Boolean = superaUmbral && diferenciaKm < 0

    fun descripcion(): String = when {
        !superaUmbral -> "Dentro de tolerancia"
        esPorExceso -> "Exceso: declaro " + String.format("%.1f", abs(diferenciaKm)) + " km de mas"
        else -> "Defecto: declaro " + String.format("%.1f", abs(diferenciaKm)) + " km de menos"
    }

    companion object {
        const val UMBRAL_PCT_DEFECTO = 10.0
        const val UMBRAL_KM_ABSOLUTO = 5.0
    }
}
