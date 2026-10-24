package com.example.teqmed.data.repository

import com.example.teqmed.data.local.ParametroSistemaDao
import com.example.teqmed.data.local.ParametroSistemaEntity
import com.example.teqmed.data.remote.ClienteTarifaCne
import com.example.teqmed.domain.model.FuenteTarifa
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.IOException

class TarifaCombustibleRepositoryTest {

    private class FakeDao(var entidadGuardada: ParametroSistemaEntity? = null) : ParametroSistemaDao {
        override suspend fun obtenerPorTipo(tipo: String): ParametroSistemaEntity? = entidadGuardada
        override fun obtenerPorTipoFlow(tipo: String): Flow<ParametroSistemaEntity?> = flowOf(entidadGuardada)
        override fun obtenerTodos(): Flow<List<ParametroSistemaEntity>> = flowOf(listOfNotNull(entidadGuardada))
        override suspend fun insertar(parametro: ParametroSistemaEntity): Long {
            entidadGuardada = parametro
            return 1L
        }
        override suspend fun actualizar(parametro: ParametroSistemaEntity) {
            entidadGuardada = parametro
        }
        override suspend fun actualizarValorPorTipo(tipo: String, valor: Double, fecha: Long) {
            entidadGuardada = entidadGuardada?.copy(valor = valor, fechaActualizacion = fecha)
        }
        override suspend fun contar(): Int = if (entidadGuardada != null) 1 else 0
    }

    private class FakeCliente(
        var responderExito: Boolean = true,
        var precioAResponder: Double = 1350.0
    ) : ClienteTarifaCne {
        override suspend fun consultarPrecioOnline(): Double {
            if (responderExito) return precioAResponder
            throw IOException("Sin conexion a red (Modo Avion)")
        }
    }

    @Test
    fun nivel1_conInternet_obtienePrecioOnlineYGuardaEnRoom() = runBlocking {
        val dao = FakeDao()
        val cliente = FakeCliente(responderExito = true, precioAResponder = 1350.0)
        val repo = TarifaCombustibleRepository(cliente, dao)

        val resultado = repo.obtenerPrecioLitroVigente()

        assertEquals(1350.0, resultado.precioLitro, 0.001)
        assertEquals(FuenteTarifa.ONLINE, resultado.fuente)
        assertNotNull(dao.entidadGuardada)
        assertEquals(1350.0, dao.entidadGuardada!!.valor, 0.001)
    }

    @Test
    fun nivel2_sinInternet_leeValorPreSembradoEnRoom() = runBlocking {
        // En Room está el valor sembrado en DatosIniciales ($1.240) o última actualización
        val dao = FakeDao(
            ParametroSistemaEntity(
                tipo = "PRECIO_COMBUSTIBLE_LITRO_CNE",
                valor = 1240.0,
                fuenteReferencia = "Comisión Nacional de Energía (CNE / Energía Abierta)"
            )
        )
        val cliente = FakeCliente(responderExito = false) // Simula desconexión de red
        val repo = TarifaCombustibleRepository(cliente, dao)

        val resultado = repo.obtenerPrecioLitroVigente()

        assertEquals(1240.0, resultado.precioLitro, 0.001)
        assertEquals(FuenteTarifa.ROOM_CACHE, resultado.fuente)
    }
}
