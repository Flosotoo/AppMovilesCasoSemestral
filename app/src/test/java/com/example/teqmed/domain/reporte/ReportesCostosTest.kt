package com.example.teqmed.domain.reporte

import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.TrasladoEntity
import com.example.teqmed.data.local.UsuarioEntity
import com.example.teqmed.data.local.VehiculoEntity
import com.example.teqmed.domain.model.FiltroPeriodo
import com.example.teqmed.domain.validator.ValidadorPeriodoReporte
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportesCostosTest {

    @Test
    fun validadorPeriodo_rechazaFechasInvalidasOInvertidas() {
        assertFalse(ValidadorPeriodoReporte.validarRango(0L, 1000L).esExitoso)
        assertFalse(ValidadorPeriodoReporte.validarRango(1000L, 0L).esExitoso)
        assertFalse(ValidadorPeriodoReporte.validarRango(2000L, 1000L).esExitoso)
    }

    @Test
    fun validadorPeriodo_aceptaRangoValido() {
        assertTrue(ValidadorPeriodoReporte.validarRango(1000L, 2000L).esExitoso)
        assertTrue(ValidadorPeriodoReporte.validarRango(1000L, 1000L).esExitoso)
    }

    @Test
    fun generarReportePorCliente_ordenaMayorAMenorCosto_y_manejaVacio() {
        val periodo = FiltroPeriodo(1000L, 5000L)
        val destinos = mapOf(
            1L to DestinoEntity(id = 1L, nombreInstitucion = "Hospital Regional", comuna = "Concepción", distanciaOficialKm = 10.0, nombreCliente = "Servicio Salud Biobío", tipoRecinto = "HOSPITAL"),
            2L to DestinoEntity(id = 2L, nombreInstitucion = "Clínica Andes", comuna = "Chillán", distanciaOficialKm = 98.0, nombreCliente = "Red Andes Salud", tipoRecinto = "CLINICA")
        )

        // Caso vacío (HU30 CA3)
        val reporteVacio = GeneradorReporteCostos.generarReportePorCliente(emptyList(), destinos, periodo)
        assertTrue(reporteVacio.estaVacio())
        assertEquals(0.0, reporteVacio.costoTotalPeriodo, 0.001)

        // Caso con traslados (HU30 CA1)
        val traslados = listOf(
            TrasladoEntity(id = "T1", tecnicoId = 1L, destinoId = 1L, vehiculoPatente = "ABCD12", odometroInicial = 100.0, odometroFinal = 110.0, timestampInicio = 2000L, costoTotal = 15000.0),
            TrasladoEntity(id = "T2", tecnicoId = 2L, destinoId = 2L, vehiculoPatente = "XYZW34", odometroInicial = 200.0, odometroFinal = 298.0, timestampInicio = 3000L, costoTotal = 85000.0),
            TrasladoEntity(id = "T3", tecnicoId = 1L, destinoId = 1L, vehiculoPatente = "ABCD12", odometroInicial = 110.0, odometroFinal = 120.0, timestampInicio = 4000L, costoTotal = 20000.0)
        )

        val reporte = GeneradorReporteCostos.generarReportePorCliente(traslados, destinos, periodo)
        assertEquals(2, reporte.items.size)
        // Red Andes Salud tiene 85.000, Servicio Salud Biobío tiene 35.000 -> Red Andes Salud primero
        assertEquals("Red Andes Salud", reporte.items[0].nombreGrupo)
        assertEquals(85000.0, reporte.items[0].costoAcumulado, 0.001)
        assertEquals("Servicio Salud Biobío", reporte.items[1].nombreGrupo)
        assertEquals(35000.0, reporte.items[1].costoAcumulado, 0.001)
        assertEquals(120000.0, reporte.costoTotalPeriodo, 0.001)
    }

    @Test
    fun generarReportePorTecnico_excluyeTecnicosSinTraslados_HU31_CA2() {
        val periodo = FiltroPeriodo(1000L, 5000L)
        val tecnicos = mapOf(
            1L to UsuarioEntity(id = 1L, rut = "11111111-1", nombre = "Juan Pérez", email = "juan@teqmed.cl", hashClave = "123", rolId = 1L),
            2L to UsuarioEntity(id = 2L, rut = "22222222-2", nombre = "María Soto", email = "maria@teqmed.cl", hashClave = "123", rolId = 1L),
            3L to UsuarioEntity(id = 3L, rut = "33333333-3", nombre = "Pedro Sin Viajes", email = "pedro@teqmed.cl", hashClave = "123", rolId = 1L)
        )

        val traslados = listOf(
            TrasladoEntity(id = "T1", tecnicoId = 1L, destinoId = 1L, vehiculoPatente = "ABCD12", odometroInicial = 100.0, odometroFinal = 110.0, timestampInicio = 2000L, costoTotal = 15000.0),
            TrasladoEntity(id = "T2", tecnicoId = 2L, destinoId = 2L, vehiculoPatente = "XYZW34", odometroInicial = 200.0, odometroFinal = 250.0, timestampInicio = 3000L, costoTotal = 45000.0)
        )

        val reporte = GeneradorReporteCostos.generarReportePorTecnico(traslados, tecnicos, periodo)
        assertEquals(2, reporte.items.size)
        assertFalse(reporte.items.any { it.nombreGrupo.contains("Pedro") })
        assertEquals("María Soto", reporte.items[0].nombreGrupo)
        assertEquals(45000.0, reporte.items[0].costoAcumulado, 0.001)
    }

    @Test
    fun generarReportePorVehiculo_excluyeVehiculosSinTraslados_HU31() {
        val periodo = FiltroPeriodo(1000L, 5000L)
        val vehiculos = mapOf(
            "ABCD12" to VehiculoEntity(patente = "ABCD12", marca = "Toyota", modelo = "Yaris", propiedad = "EMPRESA", tipoCombustible = "DIESEL", rendimientoKmL = 14.0),
            "XYZW34" to VehiculoEntity(patente = "XYZW34", marca = "Chevrolet", modelo = "Sail", propiedad = "EMPRESA", tipoCombustible = "BENZINA_95", rendimientoKmL = 15.0),
            "ZZ9999" to VehiculoEntity(patente = "ZZ9999", marca = "Nissan", modelo = "Versa", propiedad = "EMPRESA", tipoCombustible = "BENZINA_93", rendimientoKmL = 16.0)
        )

        val traslados = listOf(
            TrasladoEntity(id = "T1", tecnicoId = 1L, destinoId = 1L, vehiculoPatente = "ABCD12", odometroInicial = 100.0, odometroFinal = 120.0, timestampInicio = 2000L, costoTotal = 30000.0)
        )

        val reporte = GeneradorReporteCostos.generarReportePorVehiculo(traslados, vehiculos, periodo)
        assertEquals(1, reporte.items.size)
        assertEquals("ABCD12", reporte.items[0].identificadorGrupo)
        assertTrue(reporte.items[0].nombreGrupo.contains("Toyota Yaris"))
        assertFalse(reporte.items.any { it.identificadorGrupo == "ZZ9999" })
    }
}
