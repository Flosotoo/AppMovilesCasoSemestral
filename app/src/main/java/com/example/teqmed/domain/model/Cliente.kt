package com.example.teqmed.domain.model

enum class TipoCliente { CLINICA, HOSPITAL, CENTRO_DIALISIS }

data class Cliente(
    val id: Long = 0,
    val nombre: String,
    val tipo: TipoCliente,
    val activo: Boolean = true
)