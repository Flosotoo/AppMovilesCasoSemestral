package com.example.teqmed.domain.model

data class Destino(
    val id: Long = 0,
    val nombre: String,
    val comuna: String,
    val clienteId: Long,
    val distanciaOficialKm: Double,
    val montoPeaje: Double = 0.0,
    val activo: Boolean = true
) {
    fun tienePeaje(): Boolean = montoPeaje > 0
    fun distanciaValida(): Boolean = distanciaOficialKm > 0
    fun etiqueta(): String = "$nombre · $comuna"
}
