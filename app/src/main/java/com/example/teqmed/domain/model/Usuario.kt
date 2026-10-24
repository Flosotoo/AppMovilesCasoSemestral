package com.example.teqmed.domain.model

/** RBAC relacional (EP01). El rol llega desde las tablas roles y rol_permiso. */
data class Permiso(
    val id: Long = 0,
    val codigo: CodigoPermiso,
    val descripcion: String
)

data class Rol(
    val id: Long = 0,
    val codigo: CodigoRol,
    val nombre: String,
    val permisos: Set<CodigoPermiso> = emptySet()
) {
    fun concede(p: CodigoPermiso): Boolean = permisos.contains(p)
}

data class Usuario(
    val id: Long = 0,
    val rut: String,
    val nombre: String,
    val email: String,
    val rol: Rol,
    val activo: Boolean = true,
    val primerAcceso: Boolean = true,
    val creadoPor: Long? = null,
    val consentimientoUbicacion: Boolean = false   // RF-38
) {
    fun estaHabilitado(): Boolean = activo
    fun puede(p: CodigoPermiso): Boolean = activo && rol.concede(p)

    /** Baja logica (RF-35). Nunca se borra: dejaria traslados sin autor. */
    fun desactivar(): Usuario = copy(activo = false)
    fun activar(): Usuario = copy(activo = true)
}

/** Sesion en memoria. Nunca guarda la clave. Resuelve la navegacion (RF-37). */
data class SesionActiva(
    val usuarioId: Long,
    val nombreUsuario: String,
    val rol: CodigoRol,
    val permisos: Set<CodigoPermiso>
) {
    fun puede(p: CodigoPermiso): Boolean = permisos.contains(p)

    companion object {
        fun desde(u: Usuario) = SesionActiva(
            usuarioId = u.id,
            nombreUsuario = u.nombre,
            rol = u.rol.codigo,
            permisos = u.rol.permisos
        )
    }
}