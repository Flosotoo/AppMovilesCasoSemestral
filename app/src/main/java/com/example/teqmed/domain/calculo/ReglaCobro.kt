package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PropiedadVehiculo

sealed class ReglaCobro {

    abstract val descripcion: String
    abstract fun calcular(kilometros: Double): Double

    /** Vehículo de la empresa: se paga el combustible realmente consumido. */
    data class CobroPorConsumo(
        val rendimientoLitros100Km: Double,
        val precioPorLitro: Double
    ) : ReglaCobro() {
        override val descripcion = "Combustible consumido"

        fun litrosConsumidos(kilometros: Double): Double =
            kilometros * rendimientoLitros100Km / 100.0

        override fun calcular(kilometros: Double): Double =
            litrosConsumidos(kilometros) * precioPorLitro
    }

    /** Vehículo particular: se reembolsa una tarifa fija por kilómetro. */
    data class CobroPorReembolso(
        val tarifaPorKilometro: Double
    ) : ReglaCobro() {
        override val descripcion = "Reembolso por kilómetro"

        override fun calcular(kilometros: Double): Double =
            kilometros * tarifaPorKilometro
    }

    companion object {
        /** Único lugar donde se decide qué regla aplica. */
        fun para(
            propiedad: PropiedadVehiculo,
            rendimientoLitros100Km: Double,
            precioPorLitro: Double,
            tarifaPorKilometro: Double
        ): ReglaCobro = when (propiedad) {
            PropiedadVehiculo.EMPRESA    -> CobroPorConsumo(rendimientoLitros100Km, precioPorLitro)
            PropiedadVehiculo.PARTICULAR -> CobroPorReembolso(tarifaPorKilometro)
        }
    }
}