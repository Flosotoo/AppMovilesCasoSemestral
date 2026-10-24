package com.example.teqmed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.DestinoDao
import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.domain.validator.ValidadorDestino
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioDestinoState(
    val nombreInstitucion: String = "",
    val nombreInstitucionError: String? = null,
    val nombreCliente: String = "",
    val nombreClienteError: String? = null,
    val comuna: String = "",
    val comunaError: String? = null,
    val tipoRecinto: String = "HOSPITAL",
    val distanciaOficialKm: String = "",
    val distanciaOficialKmError: String? = null
) {
    val esValido: Boolean
        get() = nombreInstitucion.isNotBlank() &&
                nombreCliente.isNotBlank() &&
                comuna.isNotBlank() &&
                distanciaOficialKm.isNotBlank() &&
                nombreInstitucionError == null &&
                nombreClienteError == null &&
                comunaError == null &&
                distanciaOficialKmError == null

    fun validar(): FormularioDestinoState {
        val rInst = ValidadorDestino.validarTexto(nombreInstitucion, "Institución")
        val rCli = ValidadorDestino.validarTexto(nombreCliente, "Cliente")
        val rCom = ValidadorDestino.validarTexto(comuna, "Comuna")
        val rDist = ValidadorDestino.validarDistanciaOficial(distanciaOficialKm)

        return copy(
            nombreInstitucionError = rInst.mensajeError,
            nombreClienteError = rCli.mensajeError,
            comunaError = rCom.mensajeError,
            distanciaOficialKmError = rDist.mensajeError
        )
    }
}

data class DestinosAdminUiState(
    val destinos: List<DestinoEntity> = emptyList(),
    val cargando: Boolean = false,
    val mostrandoDialogoCrear: Boolean = false,
    val formulario: FormularioDestinoState = FormularioDestinoState(),
    val errorGeneral: String? = null
)

class DestinosAdminViewModel(
    private val destinoDao: DestinoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DestinosAdminUiState())
    val uiState: StateFlow<DestinosAdminUiState> = _uiState.asStateFlow()

    init {
        observarDestinos()
    }

    private fun observarDestinos() {
        viewModelScope.launch {
            destinoDao.obtenerTodos().collect { lista ->
                _uiState.update { it.copy(destinos = lista) }
            }
        }
    }

    fun abrirDialogoCrear() {
        _uiState.update {
            it.copy(mostrandoDialogoCrear = true, formulario = FormularioDestinoState(), errorGeneral = null)
        }
    }

    fun cerrarDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = false) }
    }

    fun onFormInstitucionChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(nombreInstitucion = v, nombreInstitucionError = null)) }
    fun onFormClienteChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(nombreCliente = v, nombreClienteError = null)) }
    fun onFormComunaChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(comuna = v, comunaError = null)) }
    fun onFormTipoChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(tipoRecinto = v)) }
    fun onFormDistanciaChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(distanciaOficialKm = v, distanciaOficialKmError = null)) }

    fun cambiarEstadoActivo(id: Long, nuevoActivo: Boolean) {
        viewModelScope.launch {
            destinoDao.cambiarEstado(id, nuevoActivo)
        }
    }

    fun guardarNuevoDestino() {
        val formValidado = _uiState.value.formulario.validar()
        _uiState.update { it.copy(formulario = formValidado) }

        if (!formValidado.esValido) return

        viewModelScope.launch {
            try {
                val entidad = DestinoEntity(
                    nombreInstitucion = formValidado.nombreInstitucion.trim(),
                    nombreCliente = formValidado.nombreCliente.trim(),
                    tipoRecinto = formValidado.tipoRecinto,
                    comuna = formValidado.comuna.trim(),
                    distanciaOficialKm = formValidado.distanciaOficialKm.toDouble(),
                    montoPeajeEstandar = 0,
                    activo = true
                )
                destinoDao.insertar(entidad)
                _uiState.update { it.copy(mostrandoDialogoCrear = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al guardar destino: ${e.message}") }
            }
        }
    }
}
