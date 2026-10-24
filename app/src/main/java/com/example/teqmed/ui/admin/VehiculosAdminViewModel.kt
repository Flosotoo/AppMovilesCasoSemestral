package com.example.teqmed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.VehiculoDao
import com.example.teqmed.data.local.VehiculoEntity
import com.example.teqmed.domain.validator.ValidadorVehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioVehiculoState(
    val patente: String = "",
    val patenteError: String? = null,
    val marca: String = "",
    val marcaError: String? = null,
    val modelo: String = "",
    val modeloError: String? = null,
    val propiedad: String = "EMPRESA",
    val tipoCombustible: String = "DIESEL",
    val rendimientoKmL: String = "",
    val rendimientoKmLError: String? = null
) {
    val esValido: Boolean
        get() = patente.isNotBlank() &&
                marca.isNotBlank() &&
                modelo.isNotBlank() &&
                rendimientoKmL.isNotBlank() &&
                patenteError == null &&
                marcaError == null &&
                modeloError == null &&
                rendimientoKmLError == null

    fun validar(): FormularioVehiculoState {
        val rPat = ValidadorVehiculo.validarPatente(patente)
        val rMar = ValidadorVehiculo.validarTexto(marca, "Marca")
        val rMod = ValidadorVehiculo.validarTexto(modelo, "Modelo")
        val rRen = ValidadorVehiculo.validarRendimiento(rendimientoKmL)

        return copy(
            patenteError = rPat.mensajeError,
            marcaError = rMar.mensajeError,
            modeloError = rMod.mensajeError,
            rendimientoKmLError = rRen.mensajeError
        )
    }
}

data class VehiculosAdminUiState(
    val vehiculos: List<VehiculoEntity> = emptyList(),
    val mostrandoDialogoCrear: Boolean = false,
    val formulario: FormularioVehiculoState = FormularioVehiculoState(),
    val errorGeneral: String? = null
)

class VehiculosAdminViewModel(
    private val vehiculoDao: VehiculoDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculosAdminUiState())
    val uiState: StateFlow<VehiculosAdminUiState> = _uiState.asStateFlow()

    init {
        observarVehiculos()
    }

    private fun observarVehiculos() {
        viewModelScope.launch {
            vehiculoDao.obtenerTodos().collect { lista ->
                _uiState.update { it.copy(vehiculos = lista) }
            }
        }
    }

    fun abrirDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = true, formulario = FormularioVehiculoState(), errorGeneral = null) }
    }

    fun cerrarDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = false) }
    }

    fun onFormPatenteChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(patente = v, patenteError = null)) }
    fun onFormMarcaChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(marca = v, marcaError = null)) }
    fun onFormModeloChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(modelo = v, modeloError = null)) }
    fun onFormPropiedadChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(propiedad = v)) }
    fun onFormCombustibleChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(tipoCombustible = v)) }
    fun onFormRendimientoChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(rendimientoKmL = v, rendimientoKmLError = null)) }

    fun cambiarEstadoActivo(patente: String, nuevoActivo: Boolean) {
        viewModelScope.launch {
            vehiculoDao.cambiarEstado(patente, nuevoActivo)
        }
    }

    fun guardarNuevoVehiculo() {
        val formValidado = _uiState.value.formulario.validar()
        _uiState.update { it.copy(formulario = formValidado) }

        if (!formValidado.esValido) return

        viewModelScope.launch {
            try {
                val entidad = VehiculoEntity(
                    patente = formValidado.patente.trim().uppercase(),
                    marca = formValidado.marca.trim(),
                    modelo = formValidado.modelo.trim(),
                    propiedad = formValidado.propiedad,
                    tipoCombustible = formValidado.tipoCombustible,
                    rendimientoKmL = formValidado.rendimientoKmL.toDouble(),
                    activo = true
                )
                vehiculoDao.insertar(entidad)
                _uiState.update { it.copy(mostrandoDialogoCrear = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al guardar vehículo: ${e.message}") }
            }
        }
    }
}
