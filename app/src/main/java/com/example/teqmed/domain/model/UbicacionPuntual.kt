package com.example.teqmed.domain.model

/**
 * Sello GPS de presencia (RN04, RF-06). No hay rastreo continuo:
 * solo dos capturas, una al Check-in y otra al Check-out.
 */
data class UbicacionPuntual(
    val latitud: Double,
    val longitud: Double,
    val timestampCaptura: Long,
    val momento: MomentoCaptura
) {
    fun esValida(): Boolean =
        latitud in -90.0..90.0 && longitud in -180.0..180.0

    fun resumen(): String = String.format("%.5f, %.5f", latitud, longitud)
}