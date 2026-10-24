package com.example.teqmed.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.teqmed.domain.model.CodigoRol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Modelo ligero que representa la sesión activa de un usuario (HU02, HU03).
 */
data class UsuarioSesion(
    val id: Long,
    val rut: String,
    val nombre: String,
    val email: String,
    val rol: CodigoRol,
    val consentimientoGps: Boolean
)

/**
 * Gestor de persistencia de sesión local (HU02 / HU03).
 * Mantiene la sesión abierta entre aperturas de la app mediante SharedPreferences.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _sesionActiva = MutableStateFlow<UsuarioSesion?>(cargarSesion())
    val sesionActiva: StateFlow<UsuarioSesion?> = _sesionActiva.asStateFlow()

    fun guardarSesion(
        id: Long,
        rut: String,
        nombre: String,
        email: String,
        rol: CodigoRol,
        consentimientoGps: Boolean
    ) {
        prefs.edit().apply {
            putLong(KEY_ID, id)
            putString(KEY_RUT, rut)
            putString(KEY_NOMBRE, nombre)
            putString(KEY_EMAIL, email)
            putString(KEY_ROL, rol.name)
            putBoolean(KEY_GPS_CONSENT, consentimientoGps)
            putBoolean(KEY_LOGGED_IN, true)
            apply()
        }
        _sesionActiva.value = UsuarioSesion(
            id = id,
            rut = rut,
            nombre = nombre,
            email = email,
            rol = rol,
            consentimientoGps = consentimientoGps
        )
    }

    fun actualizarConsentimientoGps(consentimiento: Boolean) {
        val actual = _sesionActiva.value ?: return
        prefs.edit().putBoolean(KEY_GPS_CONSENT, consentimiento).apply()
        _sesionActiva.value = actual.copy(consentimientoGps = consentimiento)
    }

    fun cerrarSesion() {
        prefs.edit().clear().apply()
        _sesionActiva.value = null
    }

    fun estaAutenticado(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false) && _sesionActiva.value != null

    private fun cargarSesion(): UsuarioSesion? {
        if (!prefs.getBoolean(KEY_LOGGED_IN, false)) return null

        val id = prefs.getLong(KEY_ID, -1L)
        if (id <= 0) return null

        val rut = prefs.getString(KEY_RUT, "").orEmpty()
        val nombre = prefs.getString(KEY_NOMBRE, "").orEmpty()
        val email = prefs.getString(KEY_EMAIL, "").orEmpty()
        val rolStr = prefs.getString(KEY_ROL, CodigoRol.TECNICO.name).orEmpty()
        val rol = try {
            CodigoRol.valueOf(rolStr)
        } catch (_: Exception) {
            CodigoRol.TECNICO
        }
        val consent = prefs.getBoolean(KEY_GPS_CONSENT, false)

        return UsuarioSesion(
            id = id,
            rut = rut,
            nombre = nombre,
            email = email,
            rol = rol,
            consentimientoGps = consent
        )
    }

    companion object {
        private const val PREFS_NAME = "teqruta_session_prefs"
        private const val KEY_LOGGED_IN = "is_logged_in"
        private const val KEY_ID = "user_id"
        private const val KEY_RUT = "user_rut"
        private const val KEY_NOMBRE = "user_nombre"
        private const val KEY_EMAIL = "user_email"
        private const val KEY_ROL = "user_rol"
        private const val KEY_GPS_CONSENT = "user_gps_consent"
    }
}
