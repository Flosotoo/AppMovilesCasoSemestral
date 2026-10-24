package com.example.teqmed.ui.auth

import com.example.teqmed.data.local.UsuarioSesion
import com.example.teqmed.domain.validator.ValidadorLogin

/**
 * Estado inmutable de la pantalla de inicio de sesión (HU01).
 * Todas las validaciones residen en la capa domain/validator y son orquestadas por el ViewModel.
 */
data class AuthUiState(
    val email: String = "",
    val emailError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val cargando: Boolean = false,
    val errorGeneral: String? = null,
    val usuarioAutenticado: UsuarioSesion? = null
) {
    val formularioValido: Boolean
        get() = email.isNotBlank() &&
                clave.isNotBlank() &&
                emailError == null &&
                claveError == null

    fun validar(): AuthUiState {
        val resEmail = ValidadorLogin.validarEmail(email)
        val resClave = ValidadorLogin.validarClave(clave)
        return copy(
            emailError = resEmail.mensajeError,
            claveError = resClave.mensajeError
        )
    }
}
