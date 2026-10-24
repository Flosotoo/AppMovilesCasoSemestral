package com.example.teqmed.domain.model

/**
 * Los validadores de domain/validator devuelven esto; el ViewModel lo traduce
 * a banderas del UiState.
 */
data class ValidacionResult(
    val esExitoso: Boolean,
    val mensajeError: String? = null
) {
    val esValido: Boolean get() = esExitoso

    companion object {
        fun ok(): ValidacionResult = ValidacionResult(esExitoso = true)
        fun error(mensaje: String): ValidacionResult =
            ValidacionResult(esExitoso = false, mensajeError = mensaje)
    }
}
