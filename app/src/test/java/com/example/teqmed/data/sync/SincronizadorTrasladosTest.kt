package com.example.teqmed.data.sync

import com.example.teqmed.data.local.TrasladoDao
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.util.FakeNetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeTrasladoDaoParaSync : TrasladoDao {
    val traslados = mutableMapOf<String, TrasladoEntity>()

    override fun obtenerTodos(): Flow<List<TrasladoEntity>> = flowOf(traslados.values.toList())
    override fun obtenerPorTecnico(tecnicoId: Long): Flow<List<TrasladoEntity>> = flowOf(traslados.values.filter { it.tecnicoId == tecnicoId })
    override fun obtenerJornadaHoy(tecnicoId: Long, timestampInicioDia: Long): Flow<List<TrasladoEntity>> = flowOf(traslados.values.filter { it.tecnicoId == tecnicoId && it.timestampInicio >= timestampInicioDia })
    override suspend fun obtenerTrasladoEnCurso(tecnicoId: Long): TrasladoEntity? = traslados.values.find { it.tecnicoId == tecnicoId && it.estado == "EN_CURSO" }
    override suspend fun obtenerPorId(id: String): TrasladoEntity? = traslados[id]
    override fun obtenerPorIdFlow(id: String): Flow<TrasladoEntity?> = flowOf(traslados[id])

    override fun obtenerPendientesSincronizacion(): Flow<List<TrasladoEntity>> =
        flowOf(traslados.values.filter { it.estadoSincronizacion == "PENDIENTE_OFFLINE" })

    override suspend fun obtenerListaPendientesSincronizacion(): List<TrasladoEntity> =
        traslados.values.filter { it.estadoSincronizacion == "PENDIENTE_OFFLINE" }

    override fun obtenerFinalizadosParaBandeja(): Flow<List<TrasladoEntity>> =
        flowOf(traslados.values.filter { it.estado == "FINALIZADO" })

    override suspend fun obtenerParaReportes(desde: Long, hasta: Long): List<TrasladoEntity> =
        traslados.values.filter { it.timestampInicio in desde..hasta && (it.estado == "FINALIZADO" || it.estado == "VALIDADO") }

    override fun observarParaReportes(desde: Long, hasta: Long): Flow<List<TrasladoEntity>> =
        flowOf(traslados.values.filter { it.timestampInicio in desde..hasta && (it.estado == "FINALIZADO" || it.estado == "VALIDADO") })

    override suspend fun insertar(traslado: TrasladoEntity) {
        traslados[traslado.id] = traslado
    }

    override suspend fun actualizar(traslado: TrasladoEntity) {
        traslados[traslado.id] = traslado
    }

    override suspend fun marcarSincronizado(id: String) {
        val t = traslados[id]
        if (t != null) {
            traslados[id] = t.copy(estadoSincronizacion = "SINCRONIZADO")
        }
    }

    override fun contarPendientesSincronizacionFlow(): Flow<Int> =
        flowOf(traslados.values.count { it.estadoSincronizacion == "PENDIENTE_OFFLINE" })

    override suspend fun contar(): Int = traslados.size
}

class SincronizadorTrasladosTest {

    @Test
    fun siEstaOnline_sincronizaRegistrosPendientes() = runBlocking {
        val dao = FakeTrasladoDaoParaSync()
        val network = FakeNetworkMonitor(initialOnline = true)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val sincronizador = SincronizadorTraslados(dao, network, scope)

        dao.insertar(
            TrasladoEntity(
                id = "uuid-1",
                tecnicoId = 1L,
                vehiculoPatente = "KJTR45",
                destinoId = 1L,
                timestampInicio = 1000L,
                odometroInicial = 100.0,
                estadoSincronizacion = "PENDIENTE_OFFLINE"
            )
        )

        val cant = sincronizador.sincronizarUnaVez()
        assertEquals(1, cant)
        assertEquals("SINCRONIZADO", dao.obtenerPorId("uuid-1")?.estadoSincronizacion)
    }

    @Test
    fun siEstaOffline_noSincronizaYPermanecePendiente() = runBlocking {
        val dao = FakeTrasladoDaoParaSync()
        val network = FakeNetworkMonitor(initialOnline = false)
        val scope = CoroutineScope(Dispatchers.Unconfined)
        val sincronizador = SincronizadorTraslados(dao, network, scope)

        dao.insertar(
            TrasladoEntity(
                id = "uuid-2",
                tecnicoId = 1L,
                vehiculoPatente = "KJTR45",
                destinoId = 1L,
                timestampInicio = 2000L,
                odometroInicial = 200.0,
                estadoSincronizacion = "PENDIENTE_OFFLINE"
            )
        )

        val cant = sincronizador.sincronizarUnaVez()
        assertEquals(0, cant)
        assertEquals("PENDIENTE_OFFLINE", dao.obtenerPorId("uuid-2")?.estadoSincronizacion)
    }
}
