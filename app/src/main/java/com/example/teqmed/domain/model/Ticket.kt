package com.example.teqmed.domain.model

import java.time.LocalDateTime
import kotlin.math.abs

data class Desviacion(
    val distanciaDeclarada: Double,
    val distanciaOficial: Double
) {
    val diferenciaKm: Double get() = distanciaDeclarada - distanciaOficial

    val porcentaje: Double
        get() = if (distanciaOficial <= 0.0) 0.0
        else abs(diferenciaKm) / distanciaOficial * 100.0

    fun superaUmbral(umbralPorcentaje: Double): Boolean = porcentaje > umbralPorcentaje
}

data class LineaCosto(val concepto: String, val monto: Double)

data class Ticket(
    val trasladoId: Long,
    val kilometros: Double,
    val costoMovilizacion: Double,
    val montoPeaje: Double,
    val total: Double,
    val reglaAplicada: String,
    val precioLitroAplicado: Double,
    val tarifaAplicada: Double,
    val fechaPrecio: LocalDateTime,
    val desviacion: Desviacion,
    val conDesviacion: Boolean,
    val fechaCalculo: LocalDateTime = LocalDateTime.now()
) {
    fun desglose(): List<LineaCosto> = listOf(
        LineaCosto(reglaAplicada, costoMovilizacion),
        LineaCosto("Peaje", montoPeaje),
        LineaCosto("Total", total)
    )
}