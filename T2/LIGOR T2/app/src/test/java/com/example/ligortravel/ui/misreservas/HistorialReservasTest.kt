package com.example.ligortravel.ui.misreservas

import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.reserva.aTimestampUtc
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HistorialReservasTest {

    private val hoy = LocalDate.of(2026, 10, 5)

    // Atajo para crear una reserva con solo lo que importa en estos tests.
    private fun reserva(id: String, fecha: LocalDate?) =
        Reserva(id = id, fechaViaje = fecha?.aTimestampUtc())

    @Test
    fun separaPorFecha_yHoyCuentaComoProximo() {
        val resultado = separarReservas(
            listOf(
                reserva("ayer", hoy.minusDays(1)),
                reserva("hoy", hoy),
                reserva("manana", hoy.plusDays(1))
            ),
            hoy
        )
        assertEquals(listOf("hoy", "manana"), resultado.proximas.map { it.reserva.id })
        assertEquals(listOf("ayer"), resultado.realizadas.map { it.reserva.id })
    }

    @Test
    fun proximas_vanDelMasCercanoAlMasLejano() {
        val resultado = separarReservas(
            listOf(
                reserva("lejana", hoy.plusDays(90)),
                reserva("cercana", hoy.plusDays(3)),
                reserva("media", hoy.plusDays(20))
            ),
            hoy
        )
        assertEquals(listOf("cercana", "media", "lejana"), resultado.proximas.map { it.reserva.id })
    }

    @Test
    fun realizadas_vanDeLaMasRecienteALaMasAntigua() {
        val resultado = separarReservas(
            listOf(
                reserva("antigua", hoy.minusDays(200)),
                reserva("reciente", hoy.minusDays(2)),
                reserva("media", hoy.minusDays(30))
            ),
            hoy
        )
        assertEquals(listOf("reciente", "media", "antigua"), resultado.realizadas.map { it.reserva.id })
    }

    @Test
    fun reservaSinFecha_seDescarta() {
        val resultado = separarReservas(listOf(reserva("sin_fecha", null)), hoy)
        assertTrue(resultado.proximas.isEmpty())
        assertTrue(resultado.realizadas.isEmpty())
    }

    @Test
    fun fechaGuardada_seLeeComoElMismoDia() {
        val fecha = LocalDate.of(2026, 12, 31)
        val resultado = separarReservas(listOf(reserva("x", fecha)), hoy)
        assertEquals(fecha, resultado.proximas.single().fecha)
    }

    @Test
    fun cuentaRegresiva() {
        assertEquals("Hoy", textoCuentaRegresiva(hoy, hoy))
        assertEquals("Mañana", textoCuentaRegresiva(hoy.plusDays(1), hoy))
        assertEquals("Faltan 12 días", textoCuentaRegresiva(hoy.plusDays(12), hoy))
    }

    @Test
    fun textoEstado_traduceElEstadoGuardado() {
        assertEquals("Pendiente de pago", textoEstado(Reserva.ESTADO_PENDIENTE_PAGO))
        assertEquals("Pagada", textoEstado("pagada"))
    }
}
