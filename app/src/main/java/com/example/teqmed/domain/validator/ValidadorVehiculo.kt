package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/** Reglas del mantenedor de vehiculos (RF-30, HU34). */
object ValidadorVehiculo {

    fun validarPatente(valor: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("La patente es obligatoria")
        valor.trim().length < 6 -> ValidacionResult.error("Formato invalido (ej: KJTR45)")
        else -> ValidacionResult.ok()
    }

    fun validarTexto(valor: String, campo: String): ValidacionResult = when {
        valor.isBlank() -> ValidacionResult.error("$campo es obligatorio")
        valor.trim().length < 2 -> ValidacionResult.error("Minimo 2 caracteres")
        else -> ValidacionResult.ok()
    }

    /** El rendimiento va en km/L y sostiene la formula RN02. */
    fun validarRendimiento(valor: String): ValidacionResult {
        val r = valor.toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("Ingrese el rendimiento en km/L")
            r == null -> ValidacionResult.error("Ingrese un numero valido")
            r <= 0 -> ValidacionResult.error("El rendimiento debe ser mayor a 0")
            r > 50 -> ValidacionResult.error("Valor fuera de rango (maximo 50 km/L)")
            else -> ValidacionResult.ok()
        }
    }
}