package com.example.ligortravel.ui.misreservas

import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.reserva.aTimestampUtc
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.Date

class ReglasCancelacionTest {

    private val ahora = Instant.parse("2026-11-05T15:00:00Z")
    private val hoy = LocalDate.of(2026, 11, 5)

    private fun reserva(
        estado: String = Reserva.ESTADO_PENDIENTE_PAGO,
        horasHastaViaje: Long = 72,
        id: String = "r"
    ) = Reserva(
        id = id,
        estado = estado,
        fechaViaje = Timestamp(Date.from(ahora.plus(Duration.ofHours(horasHastaViaje))))
    )

    // Reserva cuyo viaje fue (o será) en un día concreto, guardado como en la app (00:00 UTC).
    private fun reservaDelDia(fecha: LocalDate, estado: String = Reserva.ESTADO_PENDIENTE_PAGO, id: String = "r") =
        Reserva(id = id, estado = estado, fechaViaje = fecha.aTimestampUtc())

    @Test
    fun `una reserva pendiente con mas de 48 horas se puede cancelar`() {
        assertNull(ReglasCancelacion.motivoNoCancelable(reserva(horasHastaViaje = 49), ahora))
        assertTrue(ReglasCancelacion.puedeCancelarTurista(reserva(horasHastaViaje = 49), ahora))
    }

    @Test
    fun `justo a las 48 horas o menos ya no se puede cancelar`() {
        assertNotNull(ReglasCancelacion.motivoNoCancelable(reserva(horasHastaViaje = 48), ahora))
        assertNotNull(ReglasCancelacion.motivoNoCancelable(reserva(horasHastaViaje = 5), ahora))
    }

    @Test
    fun `el turista no puede cancelar una reserva pagada ni una cancelada`() {
        assertEquals(
            "Ya está pagada: para cancelarla, comunícate con la agencia",
            ReglasCancelacion.motivoNoCancelable(reserva(Reserva.ESTADO_PAGADA), ahora)
        )
        assertFalse(ReglasCancelacion.puedeCancelarTurista(reserva(Reserva.ESTADO_CANCELADA), ahora))
    }

    @Test
    fun `una reserva sin fecha no se puede cancelar`() {
        assertNotNull(ReglasCancelacion.motivoNoCancelable(Reserva(estado = Reserva.ESTADO_PENDIENTE_PAGO), ahora))
    }

    @Test
    fun `el administrador cancela cualquier reserva vigente no cancelada, sin plazo`() {
        assertTrue(ReglasCancelacion.puedeCancelarAdmin(reserva(Reserva.ESTADO_PAGADA, horasHastaViaje = 1), hoy))
        assertFalse(ReglasCancelacion.puedeCancelarAdmin(reserva(Reserva.ESTADO_CANCELADA), hoy))
    }

    @Test
    fun `solo se registra el pago de reservas pendientes`() {
        assertTrue(ReglasCancelacion.puedeRegistrarPago(reserva(), hoy))
        assertFalse(ReglasCancelacion.puedeRegistrarPago(reserva(Reserva.ESTADO_PAGADA), hoy))
        assertFalse(ReglasCancelacion.puedeRegistrarPago(reserva(Reserva.ESTADO_CANCELADA), hoy))
    }

    @Test
    fun `un viaje de ayer se muestra como realizado, uno de hoy todavia no`() {
        val deAyer = reservaDelDia(hoy.minusDays(1))
        val deHoy = reservaDelDia(hoy)

        assertEquals(Reserva.ESTADO_REALIZADA, ReglasCancelacion.estadoVisible(deAyer, hoy))
        assertEquals(Reserva.ESTADO_PENDIENTE_PAGO, ReglasCancelacion.estadoVisible(deHoy, hoy))
    }

    @Test
    fun `una cancelada sigue cancelada aunque su fecha haya pasado`() {
        val cancelada = reservaDelDia(hoy.minusDays(3), Reserva.ESTADO_CANCELADA)

        assertEquals(Reserva.ESTADO_CANCELADA, ReglasCancelacion.estadoVisible(cancelada, hoy))
    }

    @Test
    fun `un viaje realizado pendiente se considera pagado en la agencia`() {
        assertTrue(ReglasCancelacion.pagadaEnAgencia(reservaDelDia(hoy.minusDays(2)), hoy))
        assertFalse(ReglasCancelacion.pagadaEnAgencia(reservaDelDia(hoy.minusDays(2), Reserva.ESTADO_PAGADA), hoy))
        assertFalse(ReglasCancelacion.pagadaEnAgencia(reservaDelDia(hoy.plusDays(2)), hoy))
    }

    @Test
    fun `un viaje realizado ya no se cobra ni se cancela`() {
        val realizada = reservaDelDia(hoy.minusDays(1))

        assertFalse(ReglasCancelacion.puedeRegistrarPago(realizada, hoy))
        assertFalse(ReglasCancelacion.puedeCancelarAdmin(realizada, hoy))
    }

    @Test
    fun `los viajes que ya pasaron salen de pendientes y van a realizadas`() {
        val reservas = listOf(
            reservaDelDia(hoy.plusDays(20), id = "lejana"),
            reservaDelDia(hoy.minusDays(10), id = "hace10"),
            reservaDelDia(hoy.plusDays(3), id = "cercana"),
            reservaDelDia(hoy.minusDays(2), Reserva.ESTADO_PAGADA, id = "hace2pagada"),
            Reserva(id = "sinFecha", estado = Reserva.ESTADO_PENDIENTE_PAGO)
        )

        assertEquals(
            listOf("cercana", "lejana", "sinFecha"),
            ReglasCancelacion.filtrarParaAdmin(reservas, Reserva.ESTADO_PENDIENTE_PAGO, hoy).map { it.id }
        )
        // Las realizadas, de la más reciente a la más antigua.
        assertEquals(
            listOf("hace2pagada", "hace10"),
            ReglasCancelacion.filtrarParaAdmin(reservas, Reserva.ESTADO_REALIZADA, hoy).map { it.id }
        )
        assertTrue(ReglasCancelacion.filtrarParaAdmin(reservas, Reserva.ESTADO_PAGADA, hoy).isEmpty())
        assertEquals(5, ReglasCancelacion.filtrarParaAdmin(reservas, null, hoy).size)
    }
}
