package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PropiedadVehiculo

/**
 * Regla de cobro unificada segun regimen de propiedad del vehiculo (RN02).
 * Ambos regimenes calculan el costo de combustible segun consumo y tarifa CNE:
 *     C = (D / R) * P_litro
 * La diferencia es el destino contable: gasto operativo (Empresa) vs reembolso al tecnico (Particular).
 */
sealed class ReglaCobro {

    abstract val descripcion: String
    abstract fun calcularCostoBase(s: SolicitudCalculo): Double

    fun litrosConsumidos(km: Double, rendimientoKmL: Double): Double =
        if (rendimientoKmL <= 0) 0.0 else km / rendimientoKmL

    /** Vehiculo de la empresa. Costeo de combustible operativo TEQMED SpA. */
    object CobroVehiculoEmpresa : ReglaCobro() {
        override val descripcion = "Combustible operativo empresa (CNE)"

        override fun calcularCostoBase(s: SolicitudCalculo): Double =
            litrosConsumidos(s.kilometrosDeclarados, s.rendimientoKmL) * s.precioLitroVigente
    }

    /** Vehiculo particular. Reembolso directo de combustible al tecnico (CNE). */
    object CobroVehiculoParticular : ReglaCobro() {
        override val descripcion = "Reembolso combustible tecnico (CNE)"

        override fun calcularCostoBase(s: SolicitudCalculo): Double =
            litrosConsumidos(s.kilometrosDeclarados, s.rendimientoKmL) * s.precioLitroVigente
    }

    companion object {
        fun para(propiedad: PropiedadVehiculo): ReglaCobro = when (propiedad) {
            PropiedadVehiculo.EMPRESA -> CobroVehiculoEmpresa
            PropiedadVehiculo.PARTICULAR -> CobroVehiculoParticular
        }
    }
}
