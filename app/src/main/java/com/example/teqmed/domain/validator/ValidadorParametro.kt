package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ValidacionResult

/**
 * Validador puro de dominio para tarifas y parámetros del sistema (HU36, RF-32).
 * Cero dependencias de Android ni Compose.
 */
object ValidadorParametro {

    fun validarPrecioLitro(valor: String): ValidacionResult {
        val num = valor.trim().toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("El precio por litro es obligatorio")
            num == null -> ValidacionResult.error("Ingrese un número válido")
            num <= 0.0 -> ValidacionResult.error("El precio debe ser mayor a 0")
            else -> ValidacionResult.ok()
        }
    }

    /**
     * Valida el umbral porcentual entre 1% y 100% (HU36 CA2).
     */
    fun validarUmbralPorcentaje(valor: String): ValidacionResult {
        val num = valor.trim().toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("El umbral porcentual es obligatorio")
            num == null -> ValidacionResult.error("Ingrese un porcentaje válido")
            num < 1.0 || num > 100.0 -> ValidacionResult.error("El umbral debe estar entre 1% y 100%")
            else -> ValidacionResult.ok()
        }
    }

    fun validarUmbralKmAbsoluto(valor: String): ValidacionResult {
        val num = valor.trim().toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("El umbral en km es obligatorio")
            num == null -> ValidacionResult.error("Ingrese un número válido")
            num <= 0.0 -> ValidacionResult.error("El umbral debe ser mayor a 0 km")
            else -> ValidacionResult.ok()
        }
    }
}
