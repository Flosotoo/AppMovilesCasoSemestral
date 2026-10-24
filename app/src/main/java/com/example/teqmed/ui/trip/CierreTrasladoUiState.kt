package com.example.teqmed.ui.trip

import com.example.teqmed.domain.model.ItemPeaje
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.domain.validator.ValidadorTraslado

/**
 * Estado inmutable de la pantalla de Cierre de Traslado (Check-out).
 * Prohibición estricta: ninguna validación se ejecuta en Composables.
 */
data class CierreTrasladoUiState(
    val odometroInicial: Double = 0.0,
    val odometroFinal: String = "",
    val odometroFinalError: String? = null,
    val sentido: SentidoTraslado = SentidoTraslado.IDA,
    val distanciaOficialKm: Double = 0.0,

    // Lista dinámica de peajes (0 a N peajes por viaje)
    val peajes: List<ItemPeaje> = emptyList(),

    // Geolocalización discreta de presencia (Check-out)
    val latitudGpsFin: Double? = null,
    val longitudGpsFin: Double? = null,
    val capturandoGps: Boolean = false,

    // Control de UI
    val guardando: Boolean = false,
    val errorGeneral: String? = null,
    val trasladoCerradoExitoso: Boolean = false
) {
    val totalPeajes: Int
        get() = peajes.sumOf { it.montoNumerico }

    val tienePeajes: Boolean
        get() = peajes.isNotEmpty()

    /**
     * Valida el formulario completo (odómetro y cada peaje de la lista dinámica).
     */
    fun validar(): CierreTrasladoUiState {
        val resOdo = ValidadorTraslado.validarOdometroFinal(odometroFinal, odometroInicial)
        val peajesValidados = ValidadorTraslado.validarListaPeajes(peajes)

        return copy(
            odometroFinalError = resOdo.mensajeError,
            peajes = peajesValidados
        )
    }

    val formularioValido: Boolean
        get() = odometroFinalError == null &&
                odometroFinal.isNotBlank() &&
                peajes.all { it.esValido }
}
