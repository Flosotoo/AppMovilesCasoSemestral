package com.example.teqmed.data.repository

import com.example.teqmed.data.local.ParametroSistemaDao
import com.example.teqmed.data.local.ParametroSistemaEntity
import com.example.teqmed.data.remote.ClienteTarifaCne
import com.example.teqmed.domain.model.FuenteTarifa
import com.example.teqmed.domain.model.ResultadoTarifa

class TarifaCombustibleRepository(
    private val clienteCne: ClienteTarifaCne,
    private val parametroDao: ParametroSistemaDao
) {
    companion object {
        const val TIPO_PARAMETRO_CNE = "PRECIO_COMBUSTIBLE_LITRO_CNE"
    }

    /**
     * Resuelve el precio por litro vigente de combustible aplicando arquitectura
     * Offline-First limpia de 2 niveles (sin constantes quemadas en código duro):
     * 1. Nivel 1 (Online): Consulta HTTP en tiempo real a la fuente CNE.
     *    Si responde exitosamente, actualiza silenciosamente Room SQLite.
     * 2. Nivel 2 (Fallback Room SQLite): Si no hay conexión o falla la red, lee de
     *    forma transparente el valor pre-sembrado o último registrado en Room.
     */
    suspend fun obtenerPrecioLitroVigente(): ResultadoTarifa {
        // Nivel 1: Consulta Online CNE
        val onlineResult = runCatching { clienteCne.consultarPrecioOnline() }
        if (onlineResult.isSuccess) {
            val precio = onlineResult.getOrThrow()
            // Persistir de inmediato en Room para futuras sesiones offline
            val entidad = ParametroSistemaEntity(
                tipo = TIPO_PARAMETRO_CNE,
                valor = precio,
                fuenteReferencia = "CNE Online (Energía Abierta)",
                fechaActualizacion = System.currentTimeMillis()
            )
            parametroDao.insertar(entidad)
            return ResultadoTarifa(precioLitro = precio, fuente = FuenteTarifa.ONLINE)
        }

        // Nivel 2: Fallback Room SQLite local (pre-sembrado en DatosIniciales)
        val cacheRoom = parametroDao.obtenerPorTipo(TIPO_PARAMETRO_CNE)
        if (cacheRoom != null && cacheRoom.valor > 0) {
            return ResultadoTarifa(
                precioLitro = cacheRoom.valor,
                fuente = FuenteTarifa.ROOM_CACHE,
                fechaActualizacion = cacheRoom.fechaActualizacion
            )
        }

        throw IllegalStateException("No se encontró parámetro de combustible en Room SQLite")
    }
}
