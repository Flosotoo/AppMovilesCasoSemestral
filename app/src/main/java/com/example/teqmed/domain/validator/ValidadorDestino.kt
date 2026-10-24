package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/** Reglas del mantenedor de destinos (RF-31, HU35). */
object ValidadorDestino {

    fun validarTexto(valor: String, campo: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("$campo es obligatorio")
        valor.trim().length < 3 -> ValidacionResult.error("Minimo 3 caracteres")
        else -> ValidacionResult.ok()
    }

    fun validarDistanciaOficial(valor: String): ValidacionResult {
        val d = valor.toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("Ingrese la distancia oficial")
            d == null -> ValidacionResult.error("Ingrese un numero valido")
            d <= 0 -> ValidacionResult.error("La distancia debe ser mayor a 0")
            else -> ValidacionResult.ok()
        }
    }

    fun validarMontoPeaje(valor: String): ValidacionResult {
        if (valor.isBlank()) return ValidacionResult.ok()
        val m = valor.toIntOrNull()
        return when {
            m == null -> ValidacionResult.error("Ingrese un monto valido")
            m < 0 -> ValidacionResult.error("El monto no puede ser negativo")
            else -> ValidacionResult.ok()
        }
    }
}