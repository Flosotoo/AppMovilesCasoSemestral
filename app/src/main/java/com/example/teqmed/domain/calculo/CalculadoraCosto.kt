package com.example.teqmed.domain.calculo

import com.example.teqmed.domain.error.ErrorDominio
import com.example.teqmed.domain.model.Desviacion
import com.example.teqmed.domain.model.Ticket

/**
 * RNF-05 · Único punto de cálculo del sistema.
 * No conoce Room, ni Compose, ni la red: por eso se puede probar sola.
 */
object CalculadoraCosto {

    fun calcular(s: SolicitudCalculo): Result<Ticket> {

        val regla = ReglaCobro.para(
            propiedad = s.propiedad,
            rendimientoLitros100Km = s.rendimientoLitros100Km,
            precioPorLitro = s.precio.valorPorLitro,
            tarifaPorKilometro = s.tarifas.tarifaPorKilometro
        )

        val costoMovilizacion = regla.calcular(s.kilometrosDeclarados)
        val total = costoMovilizacion + s.montoPeaje

        if (total <= 0.0) return Result.failure(ErrorDominio.TarifaInvalida(total))

        val desviacion = Desviacion(s.kilometrosDeclarados, s.distanciaOficialKm)

        return Result.success(
            Ticket(
                trasladoId = s.trasladoId,
                kilometros = s.kilometrosDeclarados,
                costoMovilizacion = costoMovilizacion,
                montoPeaje = s.montoPeaje,
                total = total,
                reglaAplicada = regla.descripcion,
                precioLitroAplicado = s.precio.valorPorLitro,   // snapshot · RNF-06
                tarifaAplicada = s.tarifas.tarifaPorKilometro,  // snapshot · RNF-06
                fechaPrecio = s.precio.fechaObtencion,
                desviacion = desviacion,
                conDesviacion = desviacion.superaUmbral(s.tarifas.umbralDesviacion)
            )
        )
    }
}