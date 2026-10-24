package com.example.teqmed.domain.model

/** Consultas agregadas de Gerencia y Finanzas (EP06, RF-26 a RF-29). */

data class FiltroPeriodo(
    val timestampDesde: Long,
    val timestampHasta: Long
) {
    fun esValido(): Boolean = timestampDesde < timestampHasta
    fun contiene(t: Long): Boolean = t in timestampDesde..timestampHasta
}

enum class CriterioAgrupacion { POR_CLIENTE, POR_TECNICO, POR_VEHICULO }

data class ItemResumenCosto(
    val identificadorGrupo: String,
    val nombreGrupo: String,
    val cantidadTraslados: Int,
    val kilometrosTotales: Double,
    val costoAcumulado: Double
)

data class ResumenCosto(
    val periodo: FiltroPeriodo,
    val criterio: CriterioAgrupacion,
    val items: List<ItemResumenCosto> = emptyList()
) {
    val costoTotalPeriodo: Double = items.sumOf { it.costoAcumulado }
    val cantidadTraslados: Int = items.sumOf { it.cantidadTraslados }

    fun estaVacio(): Boolean = items.isEmpty()
    fun costoPromedio(): Double =
        if (cantidadTraslados == 0) 0.0 else costoTotalPeriodo / cantidadTraslados
}