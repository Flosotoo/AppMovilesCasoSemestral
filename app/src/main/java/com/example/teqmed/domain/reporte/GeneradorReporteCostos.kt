package com.example.teqmed.domain.reporte

import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.data.local.UsuarioEntity
import com.example.teqmed.data.local.VehiculoEntity
import com.example.teqmed.domain.model.CriterioAgrupacion
import com.example.teqmed.domain.model.FiltroPeriodo
import com.example.teqmed.domain.model.ItemResumenCosto
import com.example.teqmed.domain.model.ResumenCosto

/**
 * Motor de consolidación y agregación analítica de costos para Gerencia y Finanzas (HU30, HU31, RF-26 a RF-29).
 */
object GeneradorReporteCostos {

    /**
     * Genera el resumen consolidado agrupado por Cliente (HU30, RF-30).
     * Los clientes se ordenan de mayor a menor costo acumulado (CA1).
     * Si no hay datos, retorna ResumenCosto con items vacíos (CA3).
     */
    fun generarReportePorCliente(
        traslados: List<TrasladoEntity>,
        destinos: Map<Long, DestinoEntity>,
        periodo: FiltroPeriodo
    ): ResumenCosto {
        val grupos = traslados.groupBy { traslado ->
            val destino = destinos[traslado.destinoId]
            destino?.nombreCliente?.takeIf { it.isNotBlank() } ?: "Cliente No Asignado"
        }

        val items = grupos.map { (cliente, listaTraslados) ->
            val kmTotal = listaTraslados.sumOf { t ->
                if (t.odometroFinal != null) {
                    (t.odometroFinal - t.odometroInicial).coerceAtLeast(0.0)
                } else {
                    0.0
                }
            }
            val costoTotal = listaTraslados.sumOf { it.costoTotal ?: 0.0 }
            ItemResumenCosto(
                identificadorGrupo = cliente,
                nombreGrupo = cliente,
                cantidadTraslados = listaTraslados.size,
                kilometrosTotales = kmTotal,
                costoAcumulado = costoTotal
            )
        }.sortedByDescending { it.costoAcumulado }

        return ResumenCosto(
            periodo = periodo,
            criterio = CriterioAgrupacion.POR_CLIENTE,
            items = items
        )
    }

    /**
     * Genera el resumen consolidado agrupado por Técnico en Terreno (HU31, RF-31).
     * Excluye técnicos que no registran traslados en el período (CA2).
     */
    fun generarReportePorTecnico(
        traslados: List<TrasladoEntity>,
        tecnicos: Map<Long, UsuarioEntity>,
        periodo: FiltroPeriodo
    ): ResumenCosto {
        val grupos = traslados.groupBy { it.tecnicoId }

        val items = grupos.map { (tecnicoId, listaTraslados) ->
            val tecnico = tecnicos[tecnicoId]
            val nombre = tecnico?.nombre ?: "Técnico #$tecnicoId"
            val kmTotal = listaTraslados.sumOf { t ->
                if (t.odometroFinal != null) {
                    (t.odometroFinal - t.odometroInicial).coerceAtLeast(0.0)
                } else {
                    0.0
                }
            }
            val costoTotal = listaTraslados.sumOf { it.costoTotal ?: 0.0 }
            ItemResumenCosto(
                identificadorGrupo = tecnicoId.toString(),
                nombreGrupo = nombre,
                cantidadTraslados = listaTraslados.size,
                kilometrosTotales = kmTotal,
                costoAcumulado = costoTotal
            )
        }.sortedByDescending { it.costoAcumulado }

        return ResumenCosto(
            periodo = periodo,
            criterio = CriterioAgrupacion.POR_TECNICO,
            items = items
        )
    }

    /**
     * Genera el resumen consolidado agrupado por Vehículo (HU31, RF-31).
     * Excluye vehículos que no registran traslados en el período.
     */
    fun generarReportePorVehiculo(
        traslados: List<TrasladoEntity>,
        vehiculos: Map<String, VehiculoEntity>,
        periodo: FiltroPeriodo
    ): ResumenCosto {
        val grupos = traslados.groupBy { it.vehiculoPatente }

        val items = grupos.map { (patente, listaTraslados) ->
            val vehiculo = vehiculos[patente]
            val nombre = if (vehiculo != null) {
                "${vehiculo.marca} ${vehiculo.modelo} ($patente)"
            } else {
                "Vehículo $patente"
            }
            val kmTotal = listaTraslados.sumOf { t ->
                if (t.odometroFinal != null) {
                    (t.odometroFinal - t.odometroInicial).coerceAtLeast(0.0)
                } else {
                    0.0
                }
            }
            val costoTotal = listaTraslados.sumOf { it.costoTotal ?: 0.0 }
            ItemResumenCosto(
                identificadorGrupo = patente,
                nombreGrupo = nombre,
                cantidadTraslados = listaTraslados.size,
                kilometrosTotales = kmTotal,
                costoAcumulado = costoTotal
            )
        }.sortedByDescending { it.costoAcumulado }

        return ResumenCosto(
            periodo = periodo,
            criterio = CriterioAgrupacion.POR_VEHICULO,
            items = items
        )
    }
}
