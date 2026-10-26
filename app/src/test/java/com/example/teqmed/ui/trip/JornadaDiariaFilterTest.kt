package com.example.teqmed.ui.trip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class JornadaDiariaFilterTest {

    @Test
    fun calcularInicioDelDia_retornaMedianocheDeHoy() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val inicioDia = cal.timeInMillis

        val ahora = System.currentTimeMillis()
        assertTrue(ahora >= inicioDia)
        assertTrue(ahora - inicioDia < 24 * 3600 * 1000L)
    }

    @Test
    fun formatearHora_formateaCorrectamenteHHmm() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
        }
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val resultado = formato.format(cal.time)
        assertEquals("14:30", resultado)
    }
}
