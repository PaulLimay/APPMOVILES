package com.example.ligortravel.ui.detalle

import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.Resena
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.misreservas.ReglasCancelacion
import java.time.LocalDate
import java.util.Locale

// Reglas de las reseñas, puras (sin Firebase ni Android) y probadas en ReglasResenasTest.
// Si cambias un límite, cámbialo también en firestore.rules.
object ReglasResenas {
    const val ESTRELLAS_MIN = 1
    const val ESTRELLAS_MAX = 5
    const val COMENTARIO_MIN = 10
    const val COMENTARIO_MAX = 500

    fun validar(estrellas: Int, comentario: String): String? = when {
        estrellas !in ESTRELLAS_MIN..ESTRELLAS_MAX -> "Elige de 1 a 5 estrellas"
        comentario.trim().length < COMENTARIO_MIN ->
            "Cuéntanos un poco más: escribe al menos $COMENTARIO_MIN caracteres"
        comentario.trim().length > COMENTARIO_MAX -> "La reseña no puede pasar de $COMENTARIO_MAX caracteres"
        else -> null
    }

    fun promedio(suma: Int, total: Int): Double? = if (total <= 0) null else suma.toDouble() / total

    // "4.6" con un decimal, o "Nuevo" si el destino todavía no tiene reseñas.
    fun textoPromedio(destino: Experiencia): String =
        promedio(destino.sumaEstrellas, destino.totalResenas)
            ?.let { String.format(Locale.US, "%.1f", it) }
            ?: "Nuevo"

    // Cuántas reseñas hay de cada número de estrellas, de 5 a 1 (para las barras del resumen).
    fun distribucion(resenas: List<Resena>): Map<Int, Int> =
        (ESTRELLAS_MAX downTo ESTRELLAS_MIN).associateWith { n -> resenas.count { it.estrellas == n } }

    // Las reseñas de los demás, de la más reciente a la más antigua (la propia se muestra aparte).
    fun deOtros(resenas: List<Resena>, miUid: String?): List<Resena> =
        resenas
            .filter { it.id != miUid }
            .sortedWith(compareByDescending(nullsFirst()) { it.actualizadaEn ?: it.creadaEn })

    // Solo puede reseñar quien ya viajó: una reserva no cancelada, con la fecha pasada, de un
    // paquete que incluye este destino. Devuelve la más reciente, o null si no hay ninguna.
    fun reservaQueHabilita(
        destinoId: String,
        reservas: List<Reserva>,
        paquetes: List<Paquete>,
        hoy: LocalDate
    ): Reserva? =
        reservas
            .filter { reserva ->
                ReglasCancelacion.esRealizada(reserva, hoy) &&
                    paquetes.find { it.id == reserva.paqueteId }?.experienciaIds?.contains(destinoId) == true
            }
            .maxByOrNull { it.fechaViaje!! }

    // Para no publicar el nombre completo: "Paul Limay Ramos" -> "Paul L.".
    fun nombrePublico(nombre: String): String {
        val partes = nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
        return when (partes.size) {
            0 -> "Viajero"
            1 -> partes[0].take(40)
            else -> "${partes[0].take(36)} ${partes[1].first().uppercaseChar()}."
        }
    }
}
