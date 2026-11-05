package com.example.ligortravel.ui.reserva

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Date

// Reglas de negocio de una reserva. Están separadas del ViewModel y no dependen de Firebase ni
// de Android, así que se pueden probar con un test unitario normal (ver ReglasReservaTest).
// "hoy" se recibe como parámetro en vez de llamar a LocalDate.now() adentro: así el test puede
// fijar la fecha y siempre da el mismo resultado.
// Si cambias estos límites, cambia también las reglas de Firestore (ver CLAUDE.md).
object ReglasReserva {
    const val PERSONAS_MIN = 1
    const val PERSONAS_MAX = 10
    private const val DIAS_MAXIMOS_ANTICIPACION = 365L

    // Se reserva a partir de mañana: el mismo día no da tiempo a coordinar el viaje.
    fun fechaMinima(hoy: LocalDate): LocalDate = hoy.plusDays(1)

    fun fechaMaxima(hoy: LocalDate): LocalDate = hoy.plusDays(DIAS_MAXIMOS_ANTICIPACION)

    fun esFechaValida(fecha: LocalDate, hoy: LocalDate): Boolean =
        !fecha.isBefore(fechaMinima(hoy)) && !fecha.isAfter(fechaMaxima(hoy))

    // Devuelve el mensaje de error, o null si todo está bien (mismo estilo que
    // RegistroViewModel.validar).
    fun validar(fecha: LocalDate?, personas: Int, hoy: LocalDate): String? = when {
        fecha == null -> "Elige la fecha del viaje"
        fecha.isBefore(fechaMinima(hoy)) -> "La fecha del viaje debe ser a partir de mañana"
        fecha.isAfter(fechaMaxima(hoy)) -> "Solo puedes reservar con hasta un año de anticipación"
        personas !in PERSONAS_MIN..PERSONAS_MAX ->
            "El número de personas debe estar entre $PERSONAS_MIN y $PERSONAS_MAX"
        else -> null
    }
}

// --- Conversión de fechas ---
// Un LocalDate es "un día del calendario" sin hora ni zona horaria. El DatePicker de Material 3 y
// Firestore trabajan con instantes (milisegundos), así que usamos siempre las 00:00 en UTC como
// punto de referencia: el DatePicker también usa UTC, y así el día nunca se corre por la zona
// horaria de Perú (UTC-5).

fun LocalDate.aMillisUtc(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun millisUtcALocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun LocalDate.aTimestampUtc(): Timestamp = Timestamp(Date(aMillisUtc()))

// Camino inverso: el Timestamp guardado en Firestore vuelve a ser el día que eligió el usuario.
fun Timestamp.aLocalDateUtc(): LocalDate = millisUtcALocalDate(toDate().time)
