package com.example.teqmed.domain.model

/**
 * Flota mixta.
 * RendimientoKmL esta en KILOMETROS POR LITRO (km/L), no en L/100km.
 * La formula RN02 divide la distancia por este valor.
 */
data class Vehiculo(
    val patente: String,
    val marca: String,
    val modelo: String,
    val propiedad: PropiedadVehiculo,
    val tipoCombustible: TipoCombustible,
    val rendimientoKmL: Double,
    val activo: Boolean = true,
    val actualizadoEn: Long = System.currentTimeMillis()
) {
    fun esDeEmpresa(): Boolean = propiedad == PropiedadVehiculo.EMPRESA
    fun rendimientoValido(): Boolean = rendimientoKmL > 0
    fun etiqueta(): String = "$patente - $marca $modelo"

    fun desactivar(): Vehiculo = copy(activo = false)
    fun activar(): Vehiculo = copy(activo = true)
}