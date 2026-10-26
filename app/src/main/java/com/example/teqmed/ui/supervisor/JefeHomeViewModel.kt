package com.example.teqmed.ui.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.DestinoDao
import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.TrasladoDao
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.data.local.UsuarioDao
import com.example.teqmed.domain.model.DecisionValidacion
import com.example.teqmed.domain.model.EstadoTraslado
import com.example.teqmed.domain.validator.ValidadorResolucion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class JefeHomeUiState(
    val traslados: List<TrasladoEntity> = emptyList(),
    val destinosMap: Map<Long, DestinoEntity> = emptyMap(),
    val usuariosMap: Map<Long, String> = emptyMap(),
    val verSoloPendientes: Boolean = true,
    val trasladoParaResolver: TrasladoEntity? = null,
    val decisionSeleccionada: DecisionValidacion = DecisionValidacion.VALIDADO,
    val comentario: String = "",
    val comentarioError: String? = null,
    val guardando: Boolean = false,
    val errorGeneral: String? = null
) {
    /**
     * Bandeja activa según HU26:
     * CA1: En la bandeja aparecen los traslados finalizados del equipo.
     * CA2: Los traslados ya resueltos salen de la bandeja.
     */
    val trasladosVisibles: List<TrasladoEntity>
        get() = if (verSoloPendientes) {
            traslados.filter { it.estado == EstadoTraslado.FINALIZADO.name }
        } else {
            traslados.filter { it.estado != EstadoTraslado.FINALIZADO.name && it.estado != EstadoTraslado.EN_CURSO.name }
        }
}

class JefeHomeViewModel(
    private val trasladoDao: TrasladoDao,
    private val destinoDao: DestinoDao,
    private val usuarioDao: UsuarioDao,
    private val jefeId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(JefeHomeUiState())
    val uiState: StateFlow<JefeHomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    private fun cargarDatos() {
        viewModelScope.launch {
            trasladoDao.obtenerTodos().collect { lista ->
                _uiState.update { it.copy(traslados = lista) }
            }
        }
        viewModelScope.launch {
            destinoDao.obtenerTodos().collect { lista ->
                _uiState.update { it.copy(destinosMap = lista.associateBy { d -> d.id }) }
            }
        }
        viewModelScope.launch {
            usuarioDao.obtenerTodos().collect { usuarios ->
                _uiState.update { it.copy(usuariosMap = usuarios.associateBy({ u -> u.id }, { u -> u.nombre })) }
            }
        }
    }

    fun onFiltroPendientesChange(soloPendientes: Boolean) {
        _uiState.update { it.copy(verSoloPendientes = soloPendientes) }
    }

    fun abrirDialogoResolucion(traslado: TrasladoEntity) {
        _uiState.update {
            it.copy(
                trasladoParaResolver = traslado,
                decisionSeleccionada = DecisionValidacion.VALIDADO,
                comentario = "",
                comentarioError = null,
                errorGeneral = null
            )
        }
    }

    fun cerrarDialogoResolucion() {
        _uiState.update { it.copy(trasladoParaResolver = null) }
    }

    fun onDecisionChange(nuevaDecision: DecisionValidacion) {
        _uiState.update {
            it.copy(decisionSeleccionada = nuevaDecision, comentarioError = null)
        }
    }

    fun onComentarioChange(nuevoComentario: String) {
        _uiState.update {
            it.copy(comentario = nuevoComentario, comentarioError = null)
        }
    }

    fun resolverTraslado() {
        val traslado = _uiState.value.trasladoParaResolver ?: return
        val decision = _uiState.value.decisionSeleccionada
        val comentario = _uiState.value.comentario

        // Validador desacoplado de dominio (HU28)
        val validacion = ValidadorResolucion.validarComentario(decision, comentario)
        if (!validacion.esExitoso) {
            _uiState.update { it.copy(comentarioError = validacion.mensajeError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true) }
            try {
                val now = System.currentTimeMillis()
                val actualizado = traslado.copy(
                    estado = decision.name,
                    jefeResolucionId = jefeId,
                    decisionResolucion = decision.name,
                    comentarioResolucion = comentario.trim(),
                    timestampResolucion = now
                )
                trasladoDao.actualizar(actualizado)
                _uiState.update { it.copy(guardando = false, trasladoParaResolver = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, errorGeneral = "Error al resolver traslado: ${e.message}") }
            }
        }
    }
}
