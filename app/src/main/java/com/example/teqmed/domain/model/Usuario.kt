package com.example.teqmed.domain.model

import java.time.LocalDateTime

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
    val primerAcceso: Boolean = true
) {
    fun estaHabilitado(): Boolean = activo
    fun puede(p: CodigoPermiso): Boolean = activo && rol.concede(p)
}

/** Lo que queda en memoria tras autenticar. Nunca guarda la clave. */
data class Sesion(
    val usuarioId: Long,
    val nombre: String,
    val rol: CodigoRol,
    val permisos: Set<CodigoPermiso>,
    val iniciadaEn: LocalDateTime = LocalDateTime.now()
) {
    fun puede(p: CodigoPermiso): Boolean = permisos.contains(p)

    companion object {
        /** El rol y los permisos vienen del usuario que trajo la base. */
        fun desde(u: Usuario) = Sesion(
            usuarioId = u.id,
            nombre = u.nombre,
            rol = u.rol.codigo,
            permisos = u.rol.permisos
        )
    }
}