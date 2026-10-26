package com.example.teqmed.ui.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.ParametroSistemaDao
import com.example.teqmed.data.local.PeajeDao
import com.example.teqmed.data.local.PeajeEntity
import com.example.teqmed.data.local.TrasladoDao
import com.example.teqmed.data.repository.TarifaCombustibleRepository
import com.example.teqmed.domain.calculo.CalculadoraCosto
import com.example.teqmed.domain.calculo.SolicitudCalculo
import com.example.teqmed.domain.model.ItemPeaje
import com.example.teqmed.domain.model.PropiedadVehiculo
import com.example.teqmed.domain.model.SentidoTraslado
import com.example.teqmed.domain.model.TipoParametro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CierreTrasladoViewModel(
    private val trasladoId: String,
    private val odometroInicial: Double,
    private val distanciaOficialKm: Double,
    private val propiedadVehiculo: PropiedadVehiculo,
    private val rendimientoKmL: Double,
    private val sentido: SentidoTraslado = SentidoTraslado.IDA,
    private val tarifaRepo: TarifaCombustibleRepository? = null,
    private val parametroDao: ParametroSistemaDao? = null,
    private val trasladoDao: TrasladoDao? = null,
    private val peajeDao: PeajeDao? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CierreTrasladoUiState(
            odometroInicial = odometroInicial,
            distanciaOficialKm = distanciaOficialKm,
            sentido = sentido
        )
    )
    val uiState: StateFlow<CierreTrasladoUiState> = _uiState.asStateFlow()

    fun onOdometroFinalChange(valor: String) {
        _uiState.update { it.copy(odometroFinal = valor).validar() }
    }

    /**
     * Añade un nuevo comprobante de peaje en blanco a la lista dinámica (HU18).
     */
    fun agregarPeaje() {
        _uiState.update { current ->
            val nuevaLista = current.peajes + ItemPeaje()
            current.copy(peajes = nuevaLista)
        }
    }

    /**
     * Elimina un comprobante de peaje y recalcula la sumatoria en tiempo real.
     */
    fun eliminarPeaje(peajeId: String) {
        _uiState.update { current ->
            val filtrada = current.peajes.filter { it.id != peajeId }
            current.copy(peajes = filtrada).validar()
        }
    }

    fun onMontoPeajeChange(peajeId: String, monto: String) {
        _uiState.update { current ->
            val actualizados = current.peajes.map { item ->
                if (item.id == peajeId) item.copy(monto = monto) else item
            }
            current.copy(peajes = actualizados).validar()
        }
    }

    fun onFotoPeajeCapturada(peajeId: String, uri: String) {
        _uiState.update { current ->
            val actualizados = current.peajes.map { item ->
                if (item.id == peajeId) item.copy(fotoUri = uri) else item
            }
            current.copy(peajes = actualizados).validar()
        }
    }

    fun onUbicacionGpsCapturada(lat: Double, lng: Double) {
        _uiState.update {
            it.copy(
                latitudGpsFin = lat,
                longitudGpsFin = lng,
                capturandoGps = false
            )
        }
    }

    fun cerrarTraslado() {
        val validado = _uiState.value.validar()
        _uiState.value = validado

        if (!validado.formularioValido) return

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, errorGeneral = null) }
            try {
                val odoFin = validado.odometroFinal.toDouble()
                val distanciaDeclarada = odoFin - odometroInicial
                val montoTotalPeajes = validado.totalPeajes

                // 1. Resuelve precio combustible vía 2 niveles (Online CNE -> Fallback Room)
                // Aplica de forma unificada a todos los vehículos (Empresa y Particular - RN02)
                val tarifa = tarifaRepo?.obtenerPrecioLitroVigente()
                val precioLitro = tarifa?.precioLitro
                    ?: parametroDao?.obtenerPorTipo(TipoParametro.PRECIO_COMBUSTIBLE_LITRO_CNE.name)?.valor

                if (precioLitro == null || precioLitro <= 0.0) {
                    _uiState.update { it.copy(guardando = false, errorGeneral = "Parámetro de combustible CNE no disponible en Room") }
                    return@launch
                }

                val umbralPct = parametroDao?.obtenerPorTipo(
                    TipoParametro.UMBRAL_DESVIACION_PORCENTAJE.name
                )?.valor ?: com.example.teqmed.domain.model.Desviacion.UMBRAL_PCT_DEFECTO

                val umbralKm = parametroDao?.obtenerPorTipo(
                    TipoParametro.UMBRAL_DESVIACION_KM_ABSOLUTO.name
                )?.valor ?: com.example.teqmed.domain.model.Desviacion.UMBRAL_KM_ABSOLUTO

                // 2. Entrada al motor de cálculo algorítmico unificado (RN02)
                val solicitud = SolicitudCalculo(
                    kilometrosDeclarados = distanciaDeclarada,
                    distanciaOficialKm = distanciaOficialKm,
                    propiedad = propiedadVehiculo,
                    rendimientoKmL = rendimientoKmL,
                    montoPeaje = montoTotalPeajes,
                    precioLitroVigente = precioLitro,
                    umbralDesviacionPct = umbralPct,
                    umbralDesviacionKmAbsoluto = umbralKm
                )

                val resultadoCalculo = CalculadoraCosto.calcular(solicitud).getOrThrow()

                // 4. Persistir traslado consolidado en SQLite Room
                trasladoDao?.let { dao ->
                    val trasladoActual = dao.obtenerPorId(trasladoId)
                    if (trasladoActual != null) {
                        val actualizado = trasladoActual.copy(
                            timestampFin = System.currentTimeMillis(),
                            odometroFinal = odoFin,
                            latitudFin = validado.latitudGpsFin,
                            longitudFin = validado.longitudGpsFin,
                            timestampGpsFin = System.currentTimeMillis(),
                            montoPeajes = montoTotalPeajes,
                            costoTotal = resultadoCalculo.costoTotal,
                            precioLitroAplicado = precioLitro,
                            tarifaKmAplicada = null,
                            porcentajeDesviacion = resultadoCalculo.desviacion.porcentajeConSigno,
                            desviacionDetectada = resultadoCalculo.desviacion.superaUmbral,
                            estado = "FINALIZADO",
                            estadoSincronizacion = "PENDIENTE_OFFLINE"
                        )
                        dao.actualizar(actualizado)
                    }
                }

                // 5. Persistir cada peaje individual en la tabla relacional peajes (1:N)
                peajeDao?.let { pDao ->
                    pDao.eliminarPorTraslado(trasladoId)
                    val entidadesPeaje = validado.peajes.map { item ->
                        PeajeEntity(
                            trasladoId = trasladoId,
                            monto = item.montoNumerico,
                            fotoUri = item.fotoUri ?: ""
                        )
                    }
                    if (entidadesPeaje.isNotEmpty()) {
                        pDao.insertarTodos(entidadesPeaje)
                    }
                }

                _uiState.update { it.copy(guardando = false, trasladoCerradoExitoso = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, errorGeneral = e.message ?: "Error al cerrar traslado") }
            }
        }
    }
}
