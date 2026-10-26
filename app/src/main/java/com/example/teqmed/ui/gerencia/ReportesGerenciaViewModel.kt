package com.example.teqmed.ui.gerencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.DestinoDao
import com.example.teqmed.data.local.TrasladoDao
import com.example.teqmed.data.local.UsuarioDao
import com.example.teqmed.data.local.VehiculoDao
import com.example.teqmed.domain.model.CriterioAgrupacion
import com.example.teqmed.domain.model.FiltroPeriodo
import com.example.teqmed.domain.model.ResumenCosto
import com.example.teqmed.domain.reporte.GeneradorReporteCostos
import com.example.teqmed.domain.validator.ValidadorPeriodoReporte
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ReportesUiState(
    val timestampDesde: Long = 0L,
    val timestampHasta: Long = 0L,
    val fechaDesdeTexto: String = "",
    val fechaHastaTexto: String = "",
    val criterio: CriterioAgrupacion = CriterioAgrupacion.POR_CLIENTE,
    val resumen: ResumenCosto? = null,
    val cargando: Boolean = false,
    val errorMensaje: String? = null,
    val presetSeleccionado: String = "Mes actual"
)

class ReportesGerenciaViewModel(
    private val trasladoDao: TrasladoDao,
    private val destinoDao: DestinoDao,
    private val usuarioDao: UsuarioDao,
    private val vehiculoDao: VehiculoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportesUiState())
    val uiState: StateFlow<ReportesUiState> = _uiState.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("es", "CL"))

    init {
        aplicarPreset("Mes actual")
    }

    fun seleccionarCriterio(nuevoCriterio: CriterioAgrupacion) {
        _uiState.update { it.copy(criterio = nuevoCriterio) }
        recargar()
    }

    fun aplicarPreset(preset: String) {
        val cal = Calendar.getInstance()
        val hasta = cal.timeInMillis

        val desde = when (preset) {
            "Hoy" -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Últimos 7 días" -> {
                cal.add(Calendar.DAY_OF_YEAR, -7)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Mes actual" -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            "Todo" -> 0L
            else -> _uiState.value.timestampDesde
        }

        val calHastaFinDia = Calendar.getInstance()
        calHastaFinDia.set(Calendar.HOUR_OF_DAY, 23)
        calHastaFinDia.set(Calendar.MINUTE, 59)
        calHastaFinDia.set(Calendar.SECOND, 59)
        calHastaFinDia.set(Calendar.MILLISECOND, 999)
        val hastaFinDia = calHastaFinDia.timeInMillis

        _uiState.update {
            it.copy(
                timestampDesde = desde,
                timestampHasta = hastaFinDia,
                fechaDesdeTexto = if (desde == 0L) "Inicio Histórico" else dateFormat.format(Date(desde)),
                fechaHastaTexto = dateFormat.format(Date(hastaFinDia)),
                presetSeleccionado = preset
            )
        }
        recargar()
    }

    fun actualizarRango(desdeMillis: Long, hastaMillis: Long) {
        val validacion = ValidadorPeriodoReporte.validarRango(desdeMillis, hastaMillis)
        if (!validacion.esExitoso) {
            _uiState.update {
                it.copy(
                    errorMensaje = validacion.mensajeError,
                    resumen = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                timestampDesde = desdeMillis,
                timestampHasta = hastaMillis,
                fechaDesdeTexto = dateFormat.format(Date(desdeMillis)),
                fechaHastaTexto = dateFormat.format(Date(hastaMillis)),
                presetSeleccionado = "Personalizado",
                errorMensaje = null
            )
        }
        recargar()
    }

    fun recargar() {
        val currentState = _uiState.value
        val validacion = ValidadorPeriodoReporte.validarRango(
            if (currentState.timestampDesde == 0L) 1L else currentState.timestampDesde,
            currentState.timestampHasta
        )

        if (!validacion.esExitoso && currentState.timestampDesde != 0L) {
            _uiState.update { it.copy(errorMensaje = validacion.mensajeError, resumen = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorMensaje = null) }
            try {
                val traslados = trasladoDao.obtenerParaReportes(
                    currentState.timestampDesde,
                    currentState.timestampHasta
                )
                val destinos = destinoDao.obtenerTodos().first().associateBy { it.id }
                val usuarios = usuarioDao.obtenerTodos().first().associateBy { it.id }
                val vehiculos = vehiculoDao.obtenerTodos().first().associateBy { it.patente }

                val periodo = FiltroPeriodo(
                    timestampDesde = currentState.timestampDesde,
                    timestampHasta = currentState.timestampHasta
                )

                val resumen = when (currentState.criterio) {
                    CriterioAgrupacion.POR_CLIENTE ->
                        GeneradorReporteCostos.generarReportePorCliente(traslados, destinos, periodo)
                    CriterioAgrupacion.POR_TECNICO ->
                        GeneradorReporteCostos.generarReportePorTecnico(traslados, usuarios, periodo)
                    CriterioAgrupacion.POR_VEHICULO ->
                        GeneradorReporteCostos.generarReportePorVehiculo(traslados, vehiculos, periodo)
                }

                _uiState.update {
                    it.copy(
                        cargando = false,
                        resumen = resumen,
                        errorMensaje = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorMensaje = "Error al calcular reporte: ${e.message}"
                    )
                }
            }
        }
    }
}
