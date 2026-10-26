package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PropiedadVehiculo

/**
 * Entrada del motor de valorizacion unificado con CNE (RN02).
 */
data class SolicitudCalculo(
    val kilometrosDeclarados: Double,
    val distanciaOficialKm: Double,
    val propiedad: PropiedadVehiculo,
    val rendimientoKmL: Double,        // km por litro (tanto empresa como particular)
    val montoPeaje: Int,
    val precioLitroVigente: Double,    // precio CNE obtenido online o via fallback Room
    val tarifaReembolsoKm: Double = 0.0, // mantenido por compatibilidad
    val umbralDesviacionPct: Double = 10.0,
    val umbralDesviacionKmAbsoluto: Double = 5.0
) {
    fun esValida(): Boolean =
        kilometrosDeclarados > 0 &&
                distanciaOficialKm > 0 &&
                rendimientoKmL > 0 &&
                montoPeaje >= 0
}
