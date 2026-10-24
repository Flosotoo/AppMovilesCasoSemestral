package com.example.teqmed.domain.validator

import com.example.teqmed.domain.model.ItemPeaje
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorPeajesMultiplesTest {

    @Test
    fun listaVaciaDePeajes_esValidaYTotalEsCero() {
        val lista = emptyList<ItemPeaje>()
        val validados = ValidadorTraslado.validarListaPeajes(lista)
        val total = ValidadorTraslado.calcularTotalPeajes(validados)

        assertTrue(validados.isEmpty())
        assertEquals(0, total)
        assertTrue(validados.all { it.esValido })
    }

    @Test
    fun peajeIndividualIncompleto_marcaErrorDeMontoYFoto() {
        val incompleto = ItemPeaje(monto = "", fotoUri = null)
        val validado = ValidadorTraslado.validarItemPeaje(incompleto)

        assertFalse(validado.esValido)
        assertEquals("Ingrese el monto del peaje", validado.montoError)
        assertEquals("Debe fotografiar el ticket físico", validado.fotoError)
    }

    @Test
    fun peajeIndividualConMontoCeroONegativo_marcaError() {
        val peajeCero = ItemPeaje(monto = "0", fotoUri = "file:///foto.jpg")
        val validadoCero = ValidadorTraslado.validarItemPeaje(peajeCero)
        assertFalse(validadoCero.esValido)
        assertEquals("Debe ser mayor a $0", validadoCero.montoError)

        val peajeNeg = ItemPeaje(monto = "-500", fotoUri = "file:///foto.jpg")
        val validadoNeg = ValidadorTraslado.validarItemPeaje(peajeNeg)
        assertFalse(validadoNeg.esValido)
        assertEquals("Debe ser mayor a $0", validadoNeg.montoError)
    }

    @Test
    fun dosPeajesCompletos_sonValidosYSumatoriaEsCorrecta() {
        val p1 = ItemPeaje(monto = "2500", fotoUri = "file:///ticket1.jpg")
        val p2 = ItemPeaje(monto = "2400", fotoUri = "file:///ticket2.jpg")
        val lista = listOf(p1, p2)

        val validados = ValidadorTraslado.validarListaPeajes(lista)
        val total = ValidadorTraslado.calcularTotalPeajes(validados)

        assertTrue("Ambos peajes deben ser válidos", validados.all { it.esValido })
        assertEquals(4900, total)
        assertNull(validados[0].montoError)
        assertNull(validados[0].fotoError)
        assertNull(validados[1].montoError)
        assertNull(validados[1].fotoError)
    }

    @Test
    fun listaConUnPeajeIncompleto_marcaInvalidezGeneral() {
        val p1Valido = ItemPeaje(monto = "2500", fotoUri = "file:///ticket1.jpg")
        val p2SinFoto = ItemPeaje(monto = "2400", fotoUri = null)
        val lista = listOf(p1Valido, p2SinFoto)

        val validados = ValidadorTraslado.validarListaPeajes(lista)

        assertFalse(validados.all { it.esValido })
        assertTrue(validados[0].esValido)
        assertFalse(validados[1].esValido)
        assertNotNull(validados[1].fotoError)
    }
}
