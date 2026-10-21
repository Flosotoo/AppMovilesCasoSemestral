package com.example.teqmed.domain.model

enum class PropiedadVehiculo { EMPRESA, PARTICULAR }

data class Vehiculo(
    val id: Long = 0,
    val patente: String,
    val marca: String,
    val modelo: String,
    val propiedad: PropiedadVehiculo,
    val rendimientoLitros100Km: Double,
    val activo: Boolean = true
) {
    val esDeEmpresa: Boolean get() = propiedad == PropiedadVehiculo.EMPRESA
    fun rendimientoValido(): Boolean = rendimientoLitros100Km > 0
}
