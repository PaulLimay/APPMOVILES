package com.example.ligortravel.ui.misreservas

import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.reserva.aLocalDateUtc
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

// Condiciones para cancelar o cobrar una reserva. Puras (sin Firebase ni Android): reciben
// "ahora" por parámetro, igual que ReglasReserva recibe "hoy", para que los tests den siempre el
// mismo resultado. Si cambias el plazo, cámbialo también en firestore.rules.
object ReglasCancelacion {
    const val HORAS_ANTICIPACION = 48L

    // Devuelve por qué el turista NO puede cancelar, o null si sí puede.
    // La fecha del viaje se guarda a las 00:00 UTC del día elegido: el plazo se mide desde ahí,
    // igual que en las reglas (fechaViaje > request.time + 48 h).
    fun motivoNoCancelable(reserva: Reserva, ahora: Instant): String? {
        val inicioViaje = reserva.fechaViaje?.toDate()?.toInstant()
            ?: return "Esta reserva no tiene fecha de viaje"
        return when (reserva.estado) {
            Reserva.ESTADO_CANCELADA -> "La reserva ya está cancelada"
            Reserva.ESTADO_PAGADA -> "Ya está pagada: para cancelarla, comunícate con la agencia"
            Reserva.ESTADO_PENDIENTE_PAGO ->
                if (inicioViaje.isAfter(ahora.plus(Duration.ofHours(HORAS_ANTICIPACION)))) null
                else "Solo se puede cancelar hasta $HORAS_ANTICIPACION horas antes del viaje"
            else -> "Esta reserva no se puede cancelar"
        }
    }

    fun puedeCancelarTurista(reserva: Reserva, ahora: Instant): Boolean =
        motivoNoCancelable(reserva, ahora) == null

    // Un viaje cuya fecha ya pasó y que no se canceló se considera realizado. Igual que en
    // separarReservas, un viaje de hoy todavía cuenta como próximo.
    fun esRealizada(reserva: Reserva, hoy: LocalDate): Boolean {
        val fecha = reserva.fechaViaje?.aLocalDateUtc() ?: return false
        return reserva.estado != Reserva.ESTADO_CANCELADA && fecha.isBefore(hoy)
    }

    // Estado que se muestra: el guardado, salvo que el viaje ya se haya realizado.
    fun estadoVisible(reserva: Reserva, hoy: LocalDate): String =
        if (esRealizada(reserva, hoy)) Reserva.ESTADO_REALIZADA else reserva.estado

    // Un viaje realizado que seguía "pendiente de pago" se pagó en la agencia el día del viaje.
    fun pagadaEnAgencia(reserva: Reserva, hoy: LocalDate): Boolean =
        esRealizada(reserva, hoy) && reserva.estado == Reserva.ESTADO_PENDIENTE_PAGO

    // El administrador puede cancelar cualquier reserva que no esté cancelada ni realizada, sin
    // plazo. Un viaje que ya pasó no se cobra ni se cancela.
    fun puedeCancelarAdmin(reserva: Reserva, hoy: LocalDate): Boolean =
        reserva.estado != Reserva.ESTADO_CANCELADA && !esRealizada(reserva, hoy)

    fun puedeRegistrarPago(reserva: Reserva, hoy: LocalDate): Boolean =
        reserva.estado == Reserva.ESTADO_PENDIENTE_PAGO && !esRealizada(reserva, hoy)

    // Lista del panel: filtra por estado visible (null = todas). Las vigentes van de la fecha más
    // cercana a la más lejana; las realizadas, de la más reciente a la más antigua. Las reservas
    // sin fecha van al final.
    fun filtrarParaAdmin(reservas: List<Reserva>, estado: String?, hoy: LocalDate): List<Reserva> {
        val filtradas = reservas.filter { estado == null || estadoVisible(it, hoy) == estado }
        return if (estado == Reserva.ESTADO_REALIZADA) {
            filtradas.sortedWith(compareBy(nullsLast(reverseOrder())) { it.fechaViaje })
        } else {
            filtradas.sortedWith(compareBy(nullsLast()) { it.fechaViaje })
        }
    }
}
