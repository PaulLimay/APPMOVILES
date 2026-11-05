package com.example.ligortravel.ui.reserva

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

// Prueba las validaciones de la reserva sin emulador ni Firebase: ReglasReserva es Kotlin puro.
class ReglasReservaTest {

    // Fecha fija para que el test no dependa del día en que se ejecute.
    private val hoy = LocalDate.of(2026, 10, 5)

    @Test
    fun reservaValida_noDevuelveError() {
        assertNull(ReglasReserva.validar(hoy.plusDays(10), personas = 2, hoy = hoy))
    }

    @Test
    fun sinFecha_pideElegirFecha() {
        assertEquals("Elige la fecha del viaje", ReglasReserva.validar(null, 2, hoy))
    }

    @Test
    fun fechaDeHoy_esRechazada() {
        assertEquals(
            "La fecha del viaje debe ser a partir de mañana",
            ReglasReserva.validar(hoy, 2, hoy)
        )
    }

    @Test
    fun fechaPasada_esRechazada() {
        assertEquals(
            "La fecha del viaje debe ser a partir de mañana",
            ReglasReserva.validar(hoy.minusDays(3), 2, hoy)
        )
    }

    @Test
    fun limitesDeFecha_mananaYUnAnio_sonValidos() {
        assertNull(ReglasReserva.validar(hoy.plusDays(1), 2, hoy))
        assertNull(ReglasReserva.validar(hoy.plusDays(365), 2, hoy))
    }

    @Test
    fun fechaMasDeUnAnio_esRechazada() {
        assertEquals(
            "Solo puedes reservar con hasta un año de anticipación",
            ReglasReserva.validar(hoy.plusDays(366), 2, hoy)
        )
    }

    @Test
    fun personasFueraDeRango_sonRechazadas() {
        val esperado = "El número de personas debe estar entre 1 y 10"
        assertEquals(esperado, ReglasReserva.validar(hoy.plusDays(5), 0, hoy))
        assertEquals(esperado, ReglasReserva.validar(hoy.plusDays(5), 11, hoy))
    }

    @Test
    fun limitesDePersonas_unoYDiez_sonValidos() {
        assertNull(ReglasReserva.validar(hoy.plusDays(5), 1, hoy))
        assertNull(ReglasReserva.validar(hoy.plusDays(5), 10, hoy))
    }

    @Test
    fun conversionUtc_idaYVuelta_conservaElDia() {
        val fecha = LocalDate.of(2026, 12, 31)
        assertEquals(fecha, millisUtcALocalDate(fecha.aMillisUtc()))
    }
}
