package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/** Reglas del mantenedor de usuarios (RF-34, HU05). */
object ValidadorUsuario {

    private val PATRON_RUT = Regex("^\\d{7,8}-[\\dkK]$")

    fun validarRut(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("El RUT es obligatorio")
        !PATRON_RUT.matches(valor.trim()) ->
            ValidacionResult.error("Formato invalido (ej: 12345678-9)")
        else -> ValidacionResult.ok()
    }

    fun validarNombre(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("El nombre es obligatorio")
        valor.trim().length < 3 -> ValidacionResult.error("Minimo 3 caracteres")
        else -> ValidacionResult.ok()
    }

    fun validarEmail(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("El correo es obligatorio")
        !valor.contains("@") || !valor.contains(".") ->
            ValidacionResult.error("Ingrese un correo valido")
        else -> ValidacionResult.ok()
    }

    fun validarRol(rolId: Long?): ValidacionResult =
        if (rolId == null || rolId <= 0) ValidacionResult.error("Debe seleccionar un rol")
        else ValidacionResult.ok()
}