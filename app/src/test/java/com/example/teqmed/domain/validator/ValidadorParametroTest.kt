package com.example.teqmed.domain.validator

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorParametroTest {

    @Test
    fun validarPrecioLitro_rechazaVacioCeroONegativo() {
        assertFalse(ValidadorParametro.validarPrecioLitro("").esExitoso)
        assertFalse(ValidadorParametro.validarPrecioLitro("0").esExitoso)
        assertFalse(ValidadorParametro.validarPrecioLitro("-100").esExitoso)
        assertFalse(ValidadorParametro.validarPrecioLitro("abc").esExitoso)
    }

    @Test
    fun validarPrecioLitro_aceptaValorPositivo() {
        assertTrue(ValidadorParametro.validarPrecioLitro("1240").esExitoso)
        assertTrue(ValidadorParametro.validarPrecioLitro("1350.5").esExitoso)
    }

    @Test
    fun validarUmbralPorcentaje_validaRangoEstricto1a100() {
        assertFalse(ValidadorParametro.validarUmbralPorcentaje("0").esExitoso)
        assertFalse(ValidadorParametro.validarUmbralPorcentaje("-5").esExitoso)
        assertFalse(ValidadorParametro.validarUmbralPorcentaje("101").esExitoso)
        assertFalse(ValidadorParametro.validarUmbralPorcentaje("").esExitoso)

        assertTrue(ValidadorParametro.validarUmbralPorcentaje("1").esExitoso)
        assertTrue(ValidadorParametro.validarUmbralPorcentaje("10").esExitoso)
        assertTrue(ValidadorParametro.validarUmbralPorcentaje("100").esExitoso)
    }

    @Test
    fun validarUmbralKmAbsoluto_validaPositivo() {
        assertFalse(ValidadorParametro.validarUmbralKmAbsoluto("0").esExitoso)
        assertFalse(ValidadorParametro.validarUmbralKmAbsoluto("-1").esExitoso)
        assertFalse(ValidadorParametro.validarUmbralKmAbsoluto("").esExitoso)

        assertTrue(ValidadorParametro.validarUmbralKmAbsoluto("5").esExitoso)
        assertTrue(ValidadorParametro.validarUmbralKmAbsoluto("2.5").esExitoso)
    }
}
