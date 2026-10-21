package com.example.teqmed.domain.model

import java.time.LocalDateTime

/** Los parámetros que el administrador mantiene. Un solo lugar los define. */
data class Tarifas(
    val tarifaPorKilometro: Double,
    val umbralDesviacion: Double          // en porcentaje: 10.0 = 10%
)

data class PrecioCombustible(
    val valorPorLitro: Double,
    val fechaObtencion: LocalDateTime,
    val fuente: String = "CNE"
) {
    fun estaVigente(horas: Int = 24): Boolean =
        fechaObtencion.plusHours(horas.toLong()).isAfter(LocalDateTime.now())
}