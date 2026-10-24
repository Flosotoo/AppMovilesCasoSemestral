package com.example.teqmed.domain.model

import java.util.UUID

/**
 * Representa un comprobante de peaje individual dentro de la lista dinámica
 * de peajes de un traslado (0 a N peajes por viaje).
 */
data class ItemPeaje(
    val id: String = UUID.randomUUID().toString(),
    val monto: String = "",
    val fotoUri: String? = null,
    val montoError: String? = null,
    val fotoError: String? = null
) {
    val montoNumerico: Int
        get() = monto.toIntOrNull() ?: 0

    val esValido: Boolean
        get() = montoError == null && fotoError == null && montoNumerico > 0 && !fotoUri.isNullOrBlank()
}
