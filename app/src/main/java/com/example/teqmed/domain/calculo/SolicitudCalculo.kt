package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.model.PrecioCombustible
import com.example.teqmed.domain.model.PropiedadVehiculo
import com.example.teqmed.domain.model.Tarifas

data class SolicitudCalculo(
    val trasladoId: Long,
    val kilometrosDeclarados: Double,
    val distanciaOficialKm: Double,
    val propiedad: PropiedadVehiculo,
    val rendimientoLitros100Km: Double,
    val montoPeaje: Double,
    val tarifas: Tarifas,
    val precio: PrecioCombustible
)