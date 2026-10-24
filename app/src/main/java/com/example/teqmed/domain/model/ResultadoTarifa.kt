package com.example.teqmed.domain.model

enum class FuenteTarifa {
    ONLINE,
    ROOM_CACHE,
    CONSTANTE_DEFECTO
}

data class ResultadoTarifa(
    val precioLitro: Double,
    val fuente: FuenteTarifa,
    val fechaActualizacion: Long = System.currentTimeMillis()
)
