package com.example.teqmed.domain.model

enum class TipoCliente { CLINICA, HOSPITAL, CENTRO_DIALISIS }

data class Cliente(
    val id: Long = 0,
    val nombre: String,
    val tipo: TipoCliente,
    val activo: Boolean = true
)

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
}
