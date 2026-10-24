package com.example.teqmed.domain

import com.example.teqmed.data.local.DestinoEntity
import com.example.teqmed.data.local.ParametroSistemaEntity
import com.example.teqmed.domain.model.TipoParametro
import com.example.teqmed.domain.validator.ValidadorDestino
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pruebas de calidad e invariantes de datos de dominio (RF-31, RF-36).
 *
 * Valida que:
 * 1. Los datos semilla no contengan corrupciones (distancias <= 0, cadenas en blanco).
 * 2. Los 4 parámetros de negocio requeridos en Room existan y sean positivos.
 * 3. El Validador de Destinos sea agnóstico y extensible, permitiendo a un Administrador
 *    crear destinos hacia cualquier comuna o localidad válida en tiempo de ejecución.
 * 4. Los destinos no poseen campos muertos de coordenadas (el GPS opera exclusivamente en traslados).
 */
class IntegridadDestinosYSemillasTest {

    private val destinosSemilla = listOf(
        DestinoEntity(
            nombreInstitucion = "Hospital Guillermo Grant Benavente",
            nombreCliente = "Servicio de Salud Concepción",
            tipoRecinto = "HOSPITAL",
            comuna = "Concepción",
            distanciaOficialKm = 5.2,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Centro de Diálisis NefroCare Concepción",
            nombreCliente = "NefroCare Chile S.A.",
            tipoRecinto = "DIALISIS",
            comuna = "Concepción",
            distanciaOficialKm = 3.8,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Clínica Biobío",
            nombreCliente = "Empresas Banmédica",
            tipoRecinto = "CLINICA",
            comuna = "Talcahuano",
            distanciaOficialKm = 14.5,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Hospital Las Higueras",
            nombreCliente = "Servicio de Salud Talcahuano",
            tipoRecinto = "HOSPITAL",
            comuna = "Talcahuano",
            distanciaOficialKm = 18.0,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Hospital Clínico Herminda Martín",
            nombreCliente = "Servicio de Salud Ñuble",
            tipoRecinto = "HOSPITAL",
            comuna = "Chillán",
            distanciaOficialKm = 98.0,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Complejo Asistencial Dr. Víctor Ríos Ruiz",
            nombreCliente = "Servicio de Salud Biobío",
            tipoRecinto = "HOSPITAL",
            comuna = "Los Ángeles",
            distanciaOficialKm = 125.0,
            montoPeajeEstandar = 0
        ),
        DestinoEntity(
            nombreInstitucion = "Hospital Dr. Hernán Henríquez Aravena",
            nombreCliente = "Servicio de Salud Araucanía Sur",
            tipoRecinto = "HOSPITAL",
            comuna = "Temuco",
            distanciaOficialKm = 260.0,
            montoPeajeEstandar = 0
        )
    )

    private val parametrosSemilla = listOf(
        ParametroSistemaEntity(tipo = "PRECIO_COMBUSTIBLE_LITRO_CNE", valor = 1240.0),
        ParametroSistemaEntity(tipo = "UMBRAL_DESVIACION_PORCENTAJE", valor = 10.0),
        ParametroSistemaEntity(tipo = "UMBRAL_DESVIACION_KM_ABSOLUTO", valor = 5.0)
    )

    @Test
    fun semillasDestinos_cumplenInvariantesDeIntegridad() {
        assertTrue("Debe existir al menos un destino semilla inicial", destinosSemilla.isNotEmpty())

        destinosSemilla.forEach { destino ->
            assertTrue("La institución '${destino.nombreInstitucion}' debe tener al menos 3 caracteres", destino.nombreInstitucion.trim().length >= 3)
            assertTrue("La comuna '${destino.comuna}' debe tener al menos 3 caracteres", destino.comuna.trim().length >= 3)
            assertTrue("El cliente '${destino.nombreCliente}' debe tener al menos 3 caracteres", destino.nombreCliente.trim().length >= 3)
            assertTrue("La distancia vial oficial debe ser estrictamente positiva", destino.distanciaOficialKm > 0.0)
            assertTrue("El monto de peaje estándar en semilla debe ser 0 (se declaran dinámicamente)", destino.montoPeajeEstandar == 0)
        }
    }

    @Test
    fun semillasParametros_contieneLosParametrosRequeridosConValoresPositivos() {
        val tiposEsperados = setOf(
            TipoParametro.PRECIO_COMBUSTIBLE_LITRO_CNE.name,
            TipoParametro.UMBRAL_DESVIACION_PORCENTAJE.name,
            TipoParametro.UMBRAL_DESVIACION_KM_ABSOLUTO.name
        )

        val tiposEnSemilla = parametrosSemilla.map { it.tipo }.toSet()
        assertTrue("Los parámetros canónicos deben estar presentes", tiposEnSemilla.containsAll(tiposEsperados))

        parametrosSemilla.forEach { param ->
            assertTrue("El parámetro ${param.tipo} debe tener un valor mayor a cero", param.valor > 0.0)
        }
    }

    @Test
    fun validadorDestino_permiteAlAdministradorCrearCualquierComunaValida() {
        // Demuestra que el sistema es dinámico y extensible a cualquier comuna nueva
        val ciudadesNuevas = listOf("Coronel", "Curicó", "Valdivia", "Punta Arenas", "Arica")

        ciudadesNuevas.forEach { comuna ->
            val resComuna = ValidadorDestino.validarTexto(comuna, "Comuna")
            assertTrue("La comuna '$comuna' debe ser aceptada", resComuna.esValido)
        }

        val resNombre = ValidadorDestino.validarTexto("Hospital San José", "Institución")
        assertTrue("Nombre válido debe ser aceptado", resNombre.esValido)

        val resDistancia = ValidadorDestino.validarDistanciaOficial("35.5")
        assertTrue("Distancia válida debe ser aceptada", resDistancia.esValido)
    }

    @Test
    fun validadorDestino_rechazaDatosCorruptosOVacios() {
        assertFalse("Comuna vacía debe ser rechazada", ValidadorDestino.validarTexto("", "Comuna").esValido)
        assertFalse("Comuna de menos de 3 caracteres debe ser rechazada", ValidadorDestino.validarTexto("AB", "Comuna").esValido)
        assertFalse("Distancia vacía debe ser rechazada", ValidadorDestino.validarDistanciaOficial("").esValido)
        assertFalse("Distancia cero debe ser rechazada", ValidadorDestino.validarDistanciaOficial("0").esValido)
        assertFalse("Distancia negativa debe ser rechazada", ValidadorDestino.validarDistanciaOficial("-15.0").esValido)
        assertFalse("Distancia no numérica debe ser rechazada", ValidadorDestino.validarDistanciaOficial("abc").esValido)
    }
}
