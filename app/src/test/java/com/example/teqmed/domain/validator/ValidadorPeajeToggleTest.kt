package com.example.teqmed.domain.validator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorPeajeToggleTest {

    @Test
    fun siToggleEsFalso_noExigeMontoNiFoto() {
        val resMonto = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = false, montoStr = "")
        val resFoto = ValidadorTraslado.validarEvidenciaPeajeConToggle(huboPeaje = false, fotoUri = null)
        assertTrue(resMonto.esValido)
        assertTrue(resFoto.esValido)
    }

    @Test
    fun siToggleEsVerdadero_montoVacioEsInvalido() {
        val res = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = true, montoStr = "")
        assertFalse(res.esValido)
        assertEquals("Debe ingresar el monto pagado en peajes", res.mensajeError)
    }

    @Test
    fun siToggleEsVerdadero_montoCeroOMenorEsInvalido() {
        val resCero = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = true, montoStr = "0")
        assertFalse(resCero.esValido)
        assertEquals("El monto de peaje debe ser mayor a 0", resCero.mensajeError)

        val resNeg = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = true, montoStr = "-500")
        assertFalse(resNeg.esValido)
        assertEquals("El monto de peaje debe ser mayor a 0", resNeg.mensajeError)
    }

    @Test
    fun siToggleEsVerdadero_montoValidoPeroSinFotoEsInvalido() {
        val resMonto = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = true, montoStr = "4900")
        val resFoto = ValidadorTraslado.validarEvidenciaPeajeConToggle(huboPeaje = true, fotoUri = null)
        assertTrue(resMonto.esValido)
        assertFalse(resFoto.esValido)
        assertEquals("Debe fotografiar el comprobante del peaje", resFoto.mensajeError)
    }

    @Test
    fun siToggleEsVerdadero_montoValidoYFotoPresenteEsValido() {
        val resMonto = ValidadorTraslado.validarMontoPeajeConToggle(huboPeaje = true, montoStr = "4900")
        val resFoto = ValidadorTraslado.validarEvidenciaPeajeConToggle(huboPeaje = true, fotoUri = "file:///storage/emulated/0/Android/data/com.example.teqmed/files/peaje_123.jpg")
        assertTrue(resMonto.esValido)
        assertTrue(resFoto.esValido)
    }
}
