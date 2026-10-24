package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.error.ErrorDominio
import com.example.teqmed.domain.error.fallar
import com.example.teqmed.domain.model.PropiedadVehiculo
import com.example.teqmed.domain.model.ResultadoValorizacion

/**
 * Motor de valorizacion (EP03). Unico punto de calculo del sistema.
 *
 * No conoce Room, ni Compose, ni la red: por eso se puede probar
 * con JUnit sin levantar la app, y por eso el costo es auditable.
 *
 * RN02:
 *   Empresa     C = (D / R) * P_litro + peajes
 *   Particular  C = (D * T_km) + peajes
 *
 * Nunca lanza: todo fallo vuelve como Result.failure(ErrorDominio).
 */
object CalculadoraCosto {

    fun calcular(s: SolicitudCalculo): Result<ResultadoValorizacion> {

        if (s.rendimientoKmL <= 0 && s.propiedad == PropiedadVehiculo.EMPRESA) {
            return fallar(ErrorDominio.Negocio.RendimientoInvalido("sin patente"))
        }
        if (s.distanciaOficialKm <= 0) {
            return fallar(ErrorDominio.Negocio.DistanciaOficialNoDefinida("seleccionado"))
        }
        // Cada regimen solo necesita su propio parametro tarifario (RF-16)
        val tarifaFaltante = when (s.propiedad) {
            PropiedadVehiculo.EMPRESA -> s.precioLitroVigente <= 0
            PropiedadVehiculo.PARTICULAR -> s.tarifaReembolsoKm <= 0
        }
        if (tarifaFaltante) {
            return fallar(ErrorDominio.Negocio.TarifasNoVigentes)
        }
        if (!s.esValida()) {
            return fallar(ErrorDominio.Negocio.DatosIncompletos("kilometraje o peaje"))
        }

        val regla = ReglaCobro.para(s.propiedad)
        val costoBase = regla.calcularCostoBase(s)
        val total = costoBase + s.montoPeaje

        if (total <= 0.0) {
            return fallar(ErrorDominio.Negocio.CostoInvalido(total))
        }

        val desviacion = DetectorDesviacion.evaluar(
            declarada = s.kilometrosDeclarados,
            oficial = s.distanciaOficialKm,
            umbralPorcentaje = s.umbralDesviacionPct
        )

        return Result.success(
            ResultadoValorizacion(
                kilometrosComputados = s.kilometrosDeclarados,
                costoCombustibleOReembolso = costoBase,
                montoPeaje = s.montoPeaje,
                costoTotal = total,
                precioLitroAplicado = s.precioLitroVigente,   // congelado - RF-17
                tarifaKmAplicada = s.tarifaReembolsoKm,       // congelado - RF-17
                reglaAplicada = regla.descripcion,
                desviacion = desviacion
            )
        )
    }
}
