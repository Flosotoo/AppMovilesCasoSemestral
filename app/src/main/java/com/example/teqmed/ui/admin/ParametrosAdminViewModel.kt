package com.example.teqmed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.ParametroSistemaDao
import com.example.teqmed.data.local.ParametroSistemaEntity
import com.example.teqmed.data.repository.TarifaCombustibleRepository
import com.example.teqmed.domain.model.TipoParametro
import com.example.teqmed.domain.validator.ValidadorParametro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParametrosAdminUiState(
    val precioLitro: String = "1240",
    val precioLitroError: String? = null,
    val umbralPct: String = "10.0",
    val umbralPctError: String? = null,
    val umbralKm: String = "5.0",
    val umbralKmError: String? = null,
    val fuentePrecio: String = "",
    val cargando: Boolean = false,
    val mensajeExito: String? = null,
    val errorGeneral: String? = null
)

class ParametrosAdminViewModel(
    private val parametroDao: ParametroSistemaDao,
    private val tarifaRepo: TarifaCombustibleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ParametrosAdminUiState())
    val uiState: StateFlow<ParametrosAdminUiState> = _uiState.asStateFlow()

    init {
        cargarParametros()
    }

    private fun cargarParametros() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val pCombustible = parametroDao.obtenerPorTipo(TipoParametro.PRECIO_COMBUSTIBLE_LITRO_CNE.name)
                val pUmbralPct = parametroDao.obtenerPorTipo(TipoParametro.UMBRAL_DESVIACION_PORCENTAJE.name)
                val pUmbralKm = parametroDao.obtenerPorTipo(TipoParametro.UMBRAL_DESVIACION_KM_ABSOLUTO.name)

                _uiState.update {
                    it.copy(
                        precioLitro = pCombustible?.valor?.toInt()?.toString() ?: "1240",
                        fuentePrecio = pCombustible?.fuenteReferencia ?: "Room SQLite",
                        umbralPct = pUmbralPct?.valor?.toString() ?: "10.0",
                        umbralKm = pUmbralKm?.valor?.toString() ?: "5.0",
                        cargando = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, errorGeneral = "Error al cargar parámetros: ${e.message}") }
            }
        }
    }

    fun onPrecioLitroChange(valor: String) {
        _uiState.update { it.copy(precioLitro = valor, precioLitroError = null, mensajeExito = null, errorGeneral = null) }
    }

    fun onUmbralPctChange(valor: String) {
        _uiState.update { it.copy(umbralPct = valor, umbralPctError = null, mensajeExito = null, errorGeneral = null) }
    }

    fun onUmbralKmChange(valor: String) {
        _uiState.update { it.copy(umbralKm = valor, umbralKmError = null, mensajeExito = null, errorGeneral = null) }
    }

    fun guardarParametros() {
        val actual = _uiState.value
        val errPrecio = ValidadorParametro.validarPrecioLitro(actual.precioLitro)
        val errPct = ValidadorParametro.validarUmbralPorcentaje(actual.umbralPct)
        val errKm = ValidadorParametro.validarUmbralKmAbsoluto(actual.umbralKm)

        if (!errPrecio.esExitoso || !errPct.esExitoso || !errKm.esExitoso) {
            _uiState.update {
                it.copy(
                    precioLitroError = errPrecio.mensajeError,
                    umbralPctError = errPct.mensajeError,
                    umbralKmError = errKm.mensajeError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null, mensajeExito = null) }
            try {
                val precio = actual.precioLitro.trim().toDouble()
                val pct = actual.umbralPct.trim().toDouble()
                val km = actual.umbralKm.trim().toDouble()

                actualizarOInsertarParametro(
                    TipoParametro.PRECIO_COMBUSTIBLE_LITRO_CNE.name,
                    precio,
                    "Ajuste Manual Administrador"
                )
                actualizarOInsertarParametro(
                    TipoParametro.UMBRAL_DESVIACION_PORCENTAJE.name,
                    pct,
                    "Norma Operacional TEQMED SpA"
                )
                actualizarOInsertarParametro(
                    TipoParametro.UMBRAL_DESVIACION_KM_ABSOLUTO.name,
                    km,
                    "Norma Operacional TEQMED SpA"
                )

                _uiState.update {
                    it.copy(
                        cargando = false,
                        fuentePrecio = "Ajuste Manual Administrador",
                        mensajeExito = "Tarifas y umbrales actualizados en Room SQLite. Se aplican de inmediato sin reiniciar."
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, errorGeneral = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun sincronizarConCneOnline() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, errorGeneral = null, mensajeExito = null) }
            try {
                val resultado = tarifaRepo.sincronizarConCneOnline()
                _uiState.update {
                    it.copy(
                        cargando = false,
                        precioLitro = resultado.precioLitro.toInt().toString(),
                        fuentePrecio = "CNE Online (Energía Abierta)",
                        mensajeExito = "Precio sincronizado exitosamente desde CNE en línea ($%,d CLP/L)".format(resultado.precioLitro.toInt())
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        cargando = false,
                        errorGeneral = "No fue posible conectar con CNE en línea. Se mantiene el valor local de Room."
                    )
                }
            }
        }
    }

    private suspend fun actualizarOInsertarParametro(tipo: String, valor: Double, fuente: String) {
        val existente = parametroDao.obtenerPorTipo(tipo)
        if (existente != null) {
            parametroDao.actualizar(
                existente.copy(
                    valor = valor,
                    fuenteReferencia = fuente,
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
        } else {
            parametroDao.insertar(
                ParametroSistemaEntity(
                    tipo = tipo,
                    valor = valor,
                    fuenteReferencia = fuente,
                    fechaActualizacion = System.currentTimeMillis()
                )
            )
        }
    }
}
