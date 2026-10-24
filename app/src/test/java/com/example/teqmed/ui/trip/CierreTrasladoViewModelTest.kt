package com.example.teqmed.ui.trip

import com.example.teqmed.domain.model.ItemPeaje
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CierreTrasladoViewModelTest {

    @Test
    fun sinPeajes_formularioEsValidoConTotalCero() {
        val state = CierreTrasladoUiState(
            odometroInicial = 1000.0,
            odometroFinal = "1050",
            peajes = emptyList()
        ).validar()

        assertEquals(0, state.totalPeajes)
        assertFalse(state.tienePeajes)
        assertNull(state.odometroFinalError)
        assertTrue("Formulario sin peajes debe ser válido", state.formularioValido)
    }

    @Test
    fun conPeajeIncompleto_formularioEsInvalido() {
        val peajeIncompleto = ItemPeaje(monto = "", fotoUri = null)
        val state = CierreTrasladoUiState(
            odometroInicial = 1000.0,
            odometroFinal = "1050",
            peajes = listOf(peajeIncompleto)
        ).validar()

        assertTrue(state.tienePeajes)
        assertFalse("No debe ser válido con peaje sin monto ni foto", state.formularioValido)
        assertEquals("Ingrese el monto del peaje", state.peajes[0].montoError)
        assertEquals("Debe fotografiar el ticket físico", state.peajes[0].fotoError)
    }

    @Test
    fun conDosPeajesCompletos_calculaTotalCorrectoYEsValido() {
        val p1 = ItemPeaje(monto = "2500", fotoUri = "file:///peaje1.jpg")
        val p2 = ItemPeaje(monto = "2400", fotoUri = "file:///peaje2.jpg")

        val state = CierreTrasladoUiState(
            odometroInicial = 1000.0,
            odometroFinal = "1050",
            peajes = listOf(p1, p2)
        ).validar()

        assertEquals(4900, state.totalPeajes)
        assertTrue("Formulario con peajes completos debe ser válido", state.formularioValido)
        assertNull(state.peajes[0].montoError)
        assertNull(state.peajes[0].fotoError)
        assertNull(state.peajes[1].montoError)
        assertNull(state.peajes[1].fotoError)
    }

    @Test
    fun alEliminarPeaje_recalculaTotalAlInstante() {
        val p1 = ItemPeaje(id = "1", monto = "2500", fotoUri = "file:///peaje1.jpg")
        val p2 = ItemPeaje(id = "2", monto = "2400", fotoUri = "file:///peaje2.jpg")

        var state = CierreTrasladoUiState(
            odometroInicial = 1000.0,
            odometroFinal = "1050",
            peajes = listOf(p1, p2)
        ).validar()

        assertEquals(4900, state.totalPeajes)

        // Simula eliminación del peaje 1
        state = state.copy(peajes = state.peajes.filter { it.id != "1" }).validar()

        assertEquals(2400, state.totalPeajes)
        assertEquals(1, state.peajes.size)
        assertTrue(state.formularioValido)
    }
}
