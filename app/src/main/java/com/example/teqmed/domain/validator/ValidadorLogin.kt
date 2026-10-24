package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/** Validacion del formulario de acceso (RF-01, HU01). */
object ValidadorLogin {

    fun validarEmail(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("El correo es obligatorio")
        !valor.contains("@") || !valor.contains(".") ->
            ValidacionResult.error("Ingrese un correo valido")
        else -> ValidacionResult.ok()
    }

    fun validarClave(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("La clave es obligatoria")
        valor.length < 4 -> ValidacionResult.error("Minimo 4 caracteres")
        else -> ValidacionResult.ok()
    }
}