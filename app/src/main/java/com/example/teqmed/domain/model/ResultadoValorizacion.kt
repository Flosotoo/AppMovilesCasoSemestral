package com.example.teqmed.domain.model

/**
 * Salida de CalculadoraCosto (RF-15).
 * Conserva el precio y la tarifa usados para garantizar la
 * inmutabilidad historica exigida por RF-17.
 */
data class LineaCosto(val concepto: String, val monto: Double)

data class ResultadoValorizacion(
    val kilometrosComputados: Double,
    val costoCombustibleOReembolso: Double,
    val montoPeaje: Int,
    val costoTotal: Double,
    val precioLitroAplicado: Double,
    val tarifaKmAplicada: Double,
    val reglaAplicada: String,
    val desviacion: Desviacion
) {
    fun desglose(): List<LineaCosto> = listOf(
        LineaCosto(reglaAplicada, costoCombustibleOReembolso),
        LineaCosto("Peajes", montoPeaje.toDouble()),
        LineaCosto("Total", costoTotal)
    )
}