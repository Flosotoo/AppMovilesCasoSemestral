package com.example.teqmed.domain.model
import java.time.LocalDateTime

enum class DecisionValidacion { VALIDADO, OBSERVADO, RECHAZADO }

data class Resolucion(
    val jefeId: Long,
    val decision: DecisionValidacion,
    val comentario: String = "",
    val fecha: LocalDateTime = LocalDateTime.now()
) {
    val exigeComentario: Boolean
        get() = decision != DecisionValidacion.VALIDADO
}