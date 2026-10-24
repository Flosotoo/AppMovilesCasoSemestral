package com.example.teqmed.domain.model

/** Recinto de salud con distancia oficial tabulada desde Concepcion (RF-31). */
data class Destino(
    val id: Long = 0,
    val nombreInstitucion: String,
    val nombreCliente: String,
    val tipoRecinto: TipoCliente,
    val comuna: String,
    val latitudReferencia: Double? = null,
    val longitudReferencia: Double? = null,
    val distanciaOficialKm: Double,
    val montoPeajeEstandar: Int = 0,
    val activo: Boolean = true,
    val actualizadoEn: Long = System.currentTimeMillis()
) {
    fun tienePeaje(): Boolean = montoPeajeEstandar > 0
    fun distanciaValida(): Boolean = distanciaOficialKm > 0
    fun etiqueta(): String = "$nombreInstitucion - $comuna"

    fun desactivar(): Destino = copy(activo = false)
    fun activar(): Destino = copy(activo = true)
}