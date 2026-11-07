package com.example.ligortravel.ui.misreservas

import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.reserva.aLocalDateUtc
import java.time.LocalDate
import java.time.temporal.ChronoUnit

// Lógica pura del historial (sin Firebase ni Android), probada en HistorialReservasTest.

// Una reserva junto con su fecha ya convertida a LocalDate, para no convertirla en cada uso.
data class ReservaConFecha(val reserva: Reserva, val fecha: LocalDate)

data class ReservasSeparadas(
    val proximas: List<ReservaConFecha> = emptyList(),
    val realizadas: List<ReservaConFecha> = emptyList()
)

// Próximos viajes: la fecha es hoy o después, ordenados del más cercano al más lejano.
// Viajes realizados: la fecha ya pasó, del más reciente al más antiguo.
// Se decide solo por la fecha de inicio: un viaje de varios días que empezó ayer ya cuenta como
// realizado (no existe un estado "en curso").
fun separarReservas(reservas: List<Reserva>, hoy: LocalDate): ReservasSeparadas {
    // mapNotNull descarta reservas sin fecha. Las reglas de Firestore no permiten crearlas,
    // pero así un documento mal cargado a mano en la consola no cierra la app.
    val conFecha = reservas.mapNotNull { reserva ->
        reserva.fechaViaje?.let { ReservaConFecha(reserva, it.aLocalDateUtc()) }
    }
    // partition divide la lista en dos según la condición: (las que cumplen, las que no).
    val (proximas, realizadas) = conFecha.partition { !it.fecha.isBefore(hoy) }
    return ReservasSeparadas(
        proximas = proximas.sortedBy { it.fecha },
        realizadas = realizadas.sortedByDescending { it.fecha }
    )
}

// Texto corto para un próximo viaje: "Hoy", "Mañana" o "Faltan N días".
fun textoCuentaRegresiva(fecha: LocalDate, hoy: LocalDate): String =
    when (val dias = ChronoUnit.DAYS.between(hoy, fecha)) {
        0L -> "Hoy"
        1L -> "Mañana"
        else -> "Faltan $dias días"
    }

fun textoEstado(estado: String): String = when (estado) {
    Reserva.ESTADO_PENDIENTE_PAGO -> "Pendiente de pago"
    // Estados que aún no existen en la app (ej. si en el futuro se agrega "pagada").
    else -> estado.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
