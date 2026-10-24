package com.example.teqmed.ui.trip

import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.VehiculoEntity
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.domain.validator.ValidadorTraslado

/**
 * Estado inmutable de la pantalla de inicio de traslado en terreno (HU10).
 * Prohibición estricta: ninguna validación dentro de Composables.
 */
data class InicioTrasladoUiState(
    val sentido: SentidoTraslado = SentidoTraslado.IDA,
    val destinoSeleccionadoId: Long? = null,
    val destinoError: String? = null,

    val vehiculoSeleccionadoPatente: String? = null,
    val vehiculoError: String? = null,

    val odometroInicial: String = "",
    val odometroInicialError: String? = null,

    val destinosDisponibles: List<DestinoEntity> = emptyList(),
    val vehiculosDisponibles: List<VehiculoEntity> = emptyList(),

    val latitudGpsInicio: Double? = null,
    val longitudGpsInicio: Double? = null,
    val capturandoGps: Boolean = false,

    val guardando: Boolean = false,
    val errorGeneral: String? = null,
    val trasladoIniciadoId: String? = null
) {
    val formularioValido: Boolean
        get() = destinoSeleccionadoId != null &&
                vehiculoSeleccionadoPatente != null &&
                odometroInicial.isNotBlank() &&
                destinoError == null &&
                vehiculoError == null &&
                odometroInicialError == null

    fun validar(): InicioTrasladoUiState {
        val resDestino = ValidadorTraslado.validarSeleccionDestino(destinoSeleccionadoId)
        val resVehiculo = ValidadorTraslado.validarSeleccionVehiculo(vehiculoSeleccionadoPatente)
        val resOdo = ValidadorTraslado.validarOdometroInicial(odometroInicial)

        return copy(
            destinoError = resDestino.mensajeError,
            vehiculoError = resVehiculo.mensajeError,
            odometroInicialError = resOdo.mensajeError
        )
    }
}
