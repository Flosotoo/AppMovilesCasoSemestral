package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PropiedadVehiculo

/**
 * Regla de cobro segun regimen de propiedad del vehiculo (RN02).
 * Sealed: el conjunto es cerrado, asi el when queda exhaustivo
 * y agregar un tercer regimen rompe la compilacion donde corresponde.
 */
sealed class ReglaCobro {

    abstract val descripcion: String
    abstract fun calcularCostoBase(s: SolicitudCalculo): Double

    /**
     * Vehiculo de la empresa. Costeo por consumo real (RF-12):
     *     C = (D / R) * P_litro
     * donde R viene en km/L.
     */
    object CobroVehiculoEmpresa : ReglaCobro() {
        override val descripcion = "Combustible consumido"

        fun litrosConsumidos(km: Double, rendimientoKmL: Double): Double =
            if (rendimientoKmL <= 0) 0.0 else km / rendimientoKmL

        override fun calcularCostoBase(s: SolicitudCalculo): Double =
            litrosConsumidos(s.kilometrosDeclarados, s.rendimientoKmL) * s.precioLitroVigente
    }

    /**
     * Vehiculo particular. Reembolso kilometrico (RF-14):
     *     C = D * T_km
     */
    object CobroVehiculoParticular : ReglaCobro() {
        override val descripcion = "Reembolso por kilometro"

        override fun calcularCostoBase(s: SolicitudCalculo): Double =
            s.kilometrosDeclarados * s.tarifaReembolsoKm
    }

    companion object {
        /** Unico lugar del sistema donde se decide que regla aplica. */
        fun para(propiedad: PropiedadVehiculo): ReglaCobro = when (propiedad) {
            PropiedadVehiculo.EMPRESA -> CobroVehiculoEmpresa
            PropiedadVehiculo.PARTICULAR -> CobroVehiculoParticular
        }
    }
}