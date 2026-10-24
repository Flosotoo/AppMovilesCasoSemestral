package com.example.teqmed.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.DestinoDao
import com.example.teqmed.data.local.TrasladoDao
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.data.local.VehiculoDao
import com.example.teqmed.domain.model.EstadoSincronizacion
import com.example.teqmed.domain.model.EstadoTraslado
import com.example.teqmed.domain.model.SentidoTraslado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel para orquestar el inicio del traslado en terreno (HU10, HU11).
 */
class InicioTrasladoViewModel(
    private val destinoDao: DestinoDao,
    private val vehiculoDao: VehiculoDao,
    private val trasladoDao: TrasladoDao,
    private val tecnicoId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(InicioTrasladoUiState())
    val uiState: StateFlow<InicioTrasladoUiState> = _uiState.asStateFlow()

    init {
        cargarCatalogos()
    }

    private fun cargarCatalogos() {
        viewModelScope.launch {
            destinoDao.obtenerActivos().collect { destinos ->
                _uiState.update { it.copy(destinosDisponibles = destinos) }
            }
        }
        viewModelScope.launch {
            vehiculoDao.obtenerActivos().collect { vehiculos ->
                _uiState.update { it.copy(vehiculosDisponibles = vehiculos) }
            }
        }
    }

    fun onSentidoChange(nuevoSentido: SentidoTraslado) {
        _uiState.update { it.copy(sentido = nuevoSentido) }
    }

    fun onDestinoChange(id: Long) {
        _uiState.update { it.copy(destinoSeleccionadoId = id, destinoError = null) }
    }

    fun onVehiculoChange(patente: String) {
        _uiState.update { it.copy(vehiculoSeleccionadoPatente = patente, vehiculoError = null) }
    }

    fun onOdometroChange(valor: String) {
        _uiState.update { it.copy(odometroInicial = valor, odometroInicialError = null) }
    }

    fun onUbicacionCapturada(latitud: Double, longitud: Double) {
        _uiState.update {
            it.copy(
                latitudGpsInicio = latitud,
                longitudGpsInicio = longitud,
                capturandoGps = false
            )
        }
    }

    fun iniciarTraslado() {
        val validado = _uiState.value.validar()
        _uiState.value = validado

        if (!validado.formularioValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                val odoIni = validado.odometroInicial.toDouble()
                val nuevoId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()

                val entidad = TrasladoEntity(
                    id = nuevoId,
                    tecnicoId = tecnicoId,
                    vehiculoPatente = validado.vehiculoSeleccionadoPatente!!,
                    destinoId = validado.destinoSeleccionadoId!!,
                    sentido = validado.sentido.name,
                    timestampInicio = now,
                    odometroInicial = odoIni,
                    latitudInicio = validado.latitudGpsInicio,
                    longitudInicio = validado.longitudGpsInicio,
                    timestampGpsInicio = if (validado.latitudGpsInicio != null) now else null,
                    estado = EstadoTraslado.EN_CURSO.name,
                    estadoSincronizacion = EstadoSincronizacion.PENDIENTE_OFFLINE.name
                )

                trasladoDao.insertar(entidad)

                _uiState.update {
                    it.copy(guardando = false, trasladoIniciadoId = nuevoId)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        guardando = false,
                        errorGeneral = "Error al registrar el traslado: ${e.message}"
                    )
                }
            }
        }
    }
}
