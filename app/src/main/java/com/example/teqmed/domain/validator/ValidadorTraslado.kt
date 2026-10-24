package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ItemPeaje
import com.example.teqmed.domain.model.ValidacionResult

/**
 * Reglas del formulario de traslado (HU10, HU12, HU18).
 * Logica pura: sin Android, sin Compose, sin Room.
 */
object ValidadorTraslado {

    fun validarSeleccionDestino(id: Long?): ValidacionResult =
        if (id == null || id <= 0) ValidacionResult.error("Debe seleccionar un destino")
        else ValidacionResult.ok()

    fun validarSeleccionVehiculo(patente: String?): ValidacionResult =
        if (patente.isNullOrBlank()) ValidacionResult.error("Debe seleccionar un vehiculo")
        else ValidacionResult.ok()

    fun validarOdometroInicial(valor: String): ValidacionResult {
        val km = valor.toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("Ingrese el kilometraje inicial")
            km == null -> ValidacionResult.error("Ingrese un numero valido")
            km < 0 -> ValidacionResult.error("El kilometraje no puede ser negativo")
            else -> ValidacionResult.ok()
        }
    }

    /** El odometro final siempre debe ser mayor al inicial. */
    fun validarOdometroFinal(valor: String, odometroInicial: Double): ValidacionResult {
        val km = valor.toDoubleOrNull()
        return when {
            valor.isBlank() -> ValidacionResult.error("Ingrese el kilometraje final")
            km == null -> ValidacionResult.error("Ingrese un numero valido")
            km <= odometroInicial ->
                ValidacionResult.error("Debe ser mayor a " + odometroInicial.toInt() + " km")
            else -> ValidacionResult.ok()
        }
    }

    fun validarMontoPeaje(valor: String): ValidacionResult {
        if (valor.isBlank()) return ValidacionResult.ok()   // sin peaje es valido
        val monto = valor.toIntOrNull()
        return when {
            monto == null -> ValidacionResult.error("Ingrese un monto valido")
            monto < 0 -> ValidacionResult.error("El monto no puede ser negativo")
            else -> ValidacionResult.ok()
        }
    }

    /** RN04: si se declara peaje, la foto del comprobante es obligatoria. */
    fun validarEvidenciaPeaje(montoPeaje: String, fotoUri: String?): ValidacionResult {
        val monto = montoPeaje.toIntOrNull() ?: 0
        return if (monto > 0 && fotoUri.isNullOrBlank())
            ValidacionResult.error("Debe fotografiar el comprobante del peaje")
        else ValidacionResult.ok()
    }

    fun validarMontoPeajeConToggle(huboPeaje: Boolean, montoStr: String): ValidacionResult {
        if (!huboPeaje) return ValidacionResult.ok()
        if (montoStr.isBlank()) return ValidacionResult.error("Debe ingresar el monto pagado en peajes")
        val monto = montoStr.toIntOrNull()
        return when {
            monto == null -> ValidacionResult.error("Ingrese un monto valido")
            monto <= 0 -> ValidacionResult.error("El monto de peaje debe ser mayor a 0")
            else -> ValidacionResult.ok()
        }
    }

    fun validarEvidenciaPeajeConToggle(huboPeaje: Boolean, fotoUri: String?): ValidacionResult {
        if (!huboPeaje) return ValidacionResult.ok()
        return if (fotoUri.isNullOrBlank()) {
            ValidacionResult.error("Debe fotografiar el comprobante del peaje")
        } else {
            ValidacionResult.ok()
        }
    }

    /**
     * Valida un comprobante de peaje individual de la lista dinámica (HU18).
     * Exige monto numérico > 0 y fotografía física del ticket.
     */
    fun validarItemPeaje(item: ItemPeaje): ItemPeaje {
        val montoTrim = item.monto.trim()
        val montoInt = montoTrim.toIntOrNull()
        val errorMonto = when {
            montoTrim.isBlank() -> "Ingrese el monto del peaje"
            montoInt == null -> "Monto inválido"
            montoInt <= 0 -> "Debe ser mayor a $0"
            else -> null
        }

        val errorFoto = if (item.fotoUri.isNullOrBlank()) {
            "Debe fotografiar el ticket físico"
        } else null

        return item.copy(montoError = errorMonto, fotoError = errorFoto)
    }

    /**
     * Valida la lista dinámica completa de peajes.
     * Si la lista está vacía, es válida (el traslado no tuvo peajes).
     */
    fun validarListaPeajes(peajes: List<ItemPeaje>): List<ItemPeaje> {
        return peajes.map { validarItemPeaje(it) }
    }

    /**
     * Calcula la sumatoria consolidada de los peajes.
     */
    fun calcularTotalPeajes(peajes: List<ItemPeaje>): Int {
        return peajes.sumOf { it.montoNumerico }
    }
}
