package com.example.teqmed.domain.model

enum class CodigoRol { TECNICO, JEFE, GERENCIA, ADMIN }

enum class CodigoPermiso {
    REGISTRAR_TRASLADO, VALIDAR_TRASLADO, VER_COSTOS, ADMIN_MAESTROS, ADMIN_USUARIOS
}

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