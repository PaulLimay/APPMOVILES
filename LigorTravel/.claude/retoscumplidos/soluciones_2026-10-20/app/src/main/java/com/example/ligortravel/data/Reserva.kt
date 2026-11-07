package com.example.ligortravel.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Documento de la colección "reservas" en Firestore.
// A diferencia de Paquete (que guarda solo ids de experiencias para mostrar siempre el dato más
// reciente), una reserva es un registro histórico: guarda una COPIA del título y del precio tal
// como estaban al reservar. Si mañana el paquete cambia de precio, la reserva no debe cambiar.
// Las reglas de Firestore solo aceptan exactamente estos campos (ver CLAUDE.md).
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
    val nota: String = "",
    val opcion: Boolean = false,
    // @ServerTimestamp: si es null al guardar, Firestore pone la hora del SERVIDOR (no la del
    // celular, que el usuario podría tener mal configurada).
    @ServerTimestamp
    val creadaEn: Timestamp? = null
) {
    companion object {
        // Por ahora no hay pasarela de pago: toda reserva nace (y se queda) en este estado.
        const val ESTADO_PENDIENTE_PAGO = "pendiente_pago"
    }
}
