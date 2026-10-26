package com.example.teqmed.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqmed.data.local.RolDao
import com.example.teqmed.data.local.RolEntity
import com.example.teqmed.data.local.UsuarioDao
import com.example.teqmed.data.local.UsuarioEntity
import com.example.teqmed.domain.validator.ValidadorUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormularioUsuarioState(
    val rut: String = "",
    val rutError: String? = null,
    val nombre: String = "",
    val nombreError: String? = null,
    val email: String = "",
    val emailError: String? = null,
    val clave: String = "",
    val claveError: String? = null,
    val rolId: Long? = 1L,
    val rolError: String? = null
) {
    val esValido: Boolean
        get() = rut.isNotBlank() &&
                nombre.isNotBlank() &&
                email.isNotBlank() &&
                clave.isNotBlank() &&
                rolId != null &&
                rutError == null &&
                nombreError == null &&
                emailError == null &&
                claveError == null &&
                rolError == null

    fun validar(): FormularioUsuarioState {
        val rRut = ValidadorUsuario.validarRut(rut)
        val rNom = ValidadorUsuario.validarNombre(nombre)
        val rEml = ValidadorUsuario.validarEmail(email)
        val rRol = ValidadorUsuario.validarRol(rolId)
        val rClv = if (clave.isBlank()) com.example.teqmed.domain.model.ValidacionResult.error("La clave es obligatoria") else com.example.teqmed.domain.model.ValidacionResult.ok()

        return copy(
            rutError = rRut.mensajeError,
            nombreError = rNom.mensajeError,
            emailError = rEml.mensajeError,
            rolError = rRol.mensajeError,
            claveError = rClv.mensajeError
        )
    }
}

data class UsuariosAdminUiState(
    val usuarios: List<UsuarioEntity> = emptyList(),
    val roles: List<RolEntity> = emptyList(),
    val mostrandoDialogoCrear: Boolean = false,
    val formulario: FormularioUsuarioState = FormularioUsuarioState(),
    val errorGeneral: String? = null
)

class UsuariosAdminViewModel(
    private val usuarioDao: UsuarioDao,
    private val rolDao: RolDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosAdminUiState())
    val uiState: StateFlow<UsuariosAdminUiState> = _uiState.asStateFlow()

    init {
        observarUsuarios()
        cargarRoles()
    }

    private fun observarUsuarios() {
        viewModelScope.launch {
            usuarioDao.obtenerTodos().collect { lista ->
                _uiState.update { it.copy(usuarios = lista) }
            }
        }
    }

    private fun cargarRoles() {
        viewModelScope.launch {
            rolDao.obtenerTodos().collect { roles ->
                _uiState.update { it.copy(roles = roles) }
            }
        }
    }

    fun abrirDialogoCrear() {
        val defaultRolId = _uiState.value.roles.firstOrNull()?.id ?: 1L
        _uiState.update {
            it.copy(
                mostrandoDialogoCrear = true,
                formulario = FormularioUsuarioState(rolId = defaultRolId),
                errorGeneral = null
            )
        }
    }

    fun cerrarDialogoCrear() {
        _uiState.update { it.copy(mostrandoDialogoCrear = false) }
    }

    fun onFormRutChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(rut = v, rutError = null)) }
    fun onFormNombreChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(nombre = v, nombreError = null)) }
    fun onFormEmailChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(email = v, emailError = null)) }
    fun onFormClaveChange(v: String) = _uiState.update { it.copy(formulario = it.formulario.copy(clave = v, claveError = null)) }
    fun onFormRolChange(rolId: Long) = _uiState.update { it.copy(formulario = it.formulario.copy(rolId = rolId, rolError = null)) }

    fun cambiarEstadoActivo(id: Long, nuevoActivo: Boolean) {
        viewModelScope.launch {
            usuarioDao.cambiarEstado(id, nuevoActivo)
        }
    }

    fun guardarNuevoUsuario() {
        val formValidado = _uiState.value.formulario.validar()
        _uiState.update { it.copy(formulario = formValidado) }

        if (!formValidado.esValido) return

        viewModelScope.launch {
            try {
                val rutLimpio = formValidado.rut.trim()
                val emailLimpio = formValidado.email.trim()

                // HU05 / RF-34: Validar unicidad de RUT en Room
                val usuarioExistenteRut = usuarioDao.obtenerPorRut(rutLimpio)
                if (usuarioExistenteRut != null) {
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(rutError = "Ya existe un usuario registrado con este RUT"),
                            errorGeneral = "El RUT ingresado ya se encuentra registrado"
                        )
                    }
                    return@launch
                }

                // HU05 / RF-34: Validar unicidad de Correo Electrónico en Room
                val usuarioExistenteEmail = usuarioDao.obtenerPorEmail(emailLimpio)
                if (usuarioExistenteEmail != null) {
                    _uiState.update {
                        it.copy(
                            formulario = it.formulario.copy(emailError = "Ya existe un usuario registrado con este correo"),
                            errorGeneral = "El correo electrónico ya se encuentra registrado"
                        )
                    }
                    return@launch
                }

                val entidad = UsuarioEntity(
                    rut = rutLimpio,
                    nombre = formValidado.nombre.trim(),
                    email = emailLimpio,
                    hashClave = formValidado.clave.trim(),
                    rolId = formValidado.rolId!!,
                    activo = true,
                    primerAcceso = true,
                    consentimientoUbicacion = false
                )
                usuarioDao.insertar(entidad)
                _uiState.update { it.copy(mostrandoDialogoCrear = false, errorGeneral = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorGeneral = "Error al crear usuario: ${e.message}") }
            }
        }
    }
}
