package com.example.ligortravel.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Documento de la colección "reservas" en Firestore.
// A diferencia de Paquete (que guarda solo ids de experiencias para mostrar siempre el dato más
// reciente), una reserva es un registro histórico: guarda una COPIA del título y del precio tal
// como estaban al reservar. Si mañana el paquete cambia de precio, la reserva no debe cambiar.
// Al crearla se escriben solo los campos de la reserva; los de pago y cancelación se agregan
// después con update (ver ReservaRepository y firestore.rules).
data class Reserva(
    // Firestore genera el id al crear el documento (add); este campo no se escribe.
    @DocumentId
    val id: String = "",
    // uid de Firebase Auth del usuario que reserva. Las reglas exigen que sea el del usuario
    // autenticado, así nadie puede crear reservas a nombre de otro.
    val usuarioId: String = "",
    val paqueteId: String = "",
    val paqueteTitulo: String = "",
    // Día del viaje a las 00:00 UTC (ver aTimestampUtc en ReglasReserva.kt).
    val fechaViaje: Timestamp? = null,
    val personas: Int = 0,
    // Precio por persona al momento de reservar; las reglas comprueban que sea el del paquete.
    val precioUnitario: Int = 0,
    val total: Int = 0,
    val estado: String = ESTADO_PENDIENTE_PAGO,
    // @ServerTimestamp: si es null al guardar, Firestore pone la hora del SERVIDOR (no la del
    // celular, que el usuario podría tener mal configurada).
    @ServerTimestamp
    val creadaEn: Timestamp? = null,

    // --- Pago (cobro simulado: lo registra el administrador, no hay pasarela real) ---
    val metodoPago: String? = null,
    val pagadaEn: Timestamp? = null,

    // --- Cancelación ---
    val canceladaEn: Timestamp? = null,
    // CANCELADA_POR_USUARIO o CANCELADA_POR_ADMIN.
    val canceladaPor: String? = null,
    val motivoCancelacion: String? = null
) {
    companion object {
        // Toda reserva nace pendiente de pago; luego pasa a pagada o a cancelada.
        const val ESTADO_PENDIENTE_PAGO = "pendiente_pago"
        const val ESTADO_PAGADA = "pagada"
        const val ESTADO_CANCELADA = "cancelada"
        // No se guarda en Firestore: se calcula al mostrar una reserva cuya fecha ya pasó
        // (ver ReglasCancelacion.estadoVisible). Así no hace falta un proceso que la actualice.
        const val ESTADO_REALIZADA = "realizada"

        const val CANCELADA_POR_USUARIO = "usuario"
        const val CANCELADA_POR_ADMIN = "admin"

        // Mismos valores que acepta firestore.rules.
        val METODOS_PAGO = listOf("efectivo", "transferencia", "tarjeta", "yape_plin")
    }
}

fun textoMetodoPago(metodo: String?): String = when (metodo) {
    "efectivo" -> "Efectivo"
    "transferencia" -> "Transferencia"
    "tarjeta" -> "Tarjeta"
    "yape_plin" -> "Yape / Plin"
    else -> "Sin registrar"
}
