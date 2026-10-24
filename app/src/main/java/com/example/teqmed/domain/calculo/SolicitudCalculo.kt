package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PropiedadVehiculo

/**
 * Entrada del motor de valorizacion. Trae TODO lo que el calculo necesita,
 * de modo que CalculadoraCosto no consulta base de datos ni red.
 */
data class SolicitudCalculo(
    val kilometrosDeclarados: Double,
    val distanciaOficialKm: Double,
    val propiedad: PropiedadVehiculo,
    val rendimientoKmL: Double,        // km por litro (NO litros/100km)
    val montoPeaje: Int,
    val precioLitroVigente: Double,
    val tarifaReembolsoKm: Double,
    val umbralDesviacionPct: Double = 10.0
) {
    fun esValida(): Boolean =
        kilometrosDeclarados > 0 &&
                distanciaOficialKm > 0 &&
                rendimientoKmL > 0 &&
                montoPeaje >= 0
}