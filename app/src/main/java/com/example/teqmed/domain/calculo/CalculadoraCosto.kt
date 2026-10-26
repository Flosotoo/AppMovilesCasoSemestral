package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.error.ErrorDominio
import com.example.teqmed.domain.error.fallar
import com.example.teqmed.domain.model.ResultadoValorizacion

/**
 * Motor de valorizacion unificado (EP03, RN02).
 * Formula unica de combustible para todos los vehiculos:
 *     C = (D / R) * P_litro_CNE + peajes
 */
object CalculadoraCosto {

    fun calcular(s: SolicitudCalculo): Result<ResultadoValorizacion> {

        if (s.rendimientoKmL <= 0) {
            return fallar(ErrorDominio.Negocio.RendimientoInvalido("sin patente"))
        }
        if (s.distanciaOficialKm <= 0) {
            return fallar(ErrorDominio.Negocio.DistanciaOficialNoDefinida("seleccionado"))
        }
        if (s.precioLitroVigente <= 0) {
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
            umbralPorcentaje = s.umbralDesviacionPct,
            umbralKmAbsoluto = s.umbralDesviacionKmAbsoluto
        )

        return Result.success(
            ResultadoValorizacion(
                kilometrosComputados = s.kilometrosDeclarados,
                costoCombustibleOReembolso = costoBase,
                montoPeaje = s.montoPeaje,
                costoTotal = total,
                precioLitroAplicado = s.precioLitroVigente,   // congelado CNE - RF-17
                tarifaKmAplicada = 0.0,
                reglaAplicada = regla.descripcion,
                desviacion = desviacion
            )
        )
    }
}
