package com.example.teqmed.domain.model

/** Resolucion del jefe de servicio sobre un traslado (RF-24). */
data class ResolucionJefatura(
    val jefeId: Long,
    val decision: DecisionValidacion,
    val comentario: String = "",
    val timestampResolucion: Long = System.currentTimeMillis()
) {
    fun exigeComentario(): Boolean = decision != DecisionValidacion.VALIDADO
    fun esValida(): Boolean = !exigeComentario() || comentario.isNotBlank()
}