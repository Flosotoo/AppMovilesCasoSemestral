package com.example.teqmed.ui.trip

import com.example.teqmed.domain.calculo.DetectorDesviacion
import com.example.teqmed.domain.model.ItemPeaje
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.domain.validator.ValidadorTraslado
import java.util.Locale

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

    // Feedback de desvío en tiempo real (HU20 -> RF-11)
    val alertaDesvio: String? = null,
    val desvioSuperaUmbral: Boolean = false,

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

        var alerta: String? = null
        var supera = false

        val odoNum = odometroFinal.toDoubleOrNull()
        if (resOdo.esValido && odoNum != null && odoNum > odometroInicial && distanciaOficialKm > 0) {
            val distDeclarada = odoNum - odometroInicial
            val desv = DetectorDesviacion.evaluar(
                declarada = distDeclarada,
                oficial = distanciaOficialKm
            )
            val pctStr = String.format(Locale.US, "%.1f", desv.porcentajeConSigno)
            if (desv.superaUmbral) {
                alerta = "Alerta de Desvío (+$pctStr%) - El kilometraje declarado supera el umbral permitido del 10%. Pasará a revisión de jefatura"
                supera = true
            } else {
                alerta = "Kilometraje dentro del rango esperado (+$pctStr%)"
                supera = false
            }
        }

        return copy(
            odometroFinalError = resOdo.mensajeError,
            alertaDesvio = alerta,
            desvioSuperaUmbral = supera,
            peajes = peajesValidados
        )
    }

    val formularioValido: Boolean
        get() = odometroFinalError == null &&
                odometroFinal.isNotBlank() &&
                peajes.all { it.esValido }
}
