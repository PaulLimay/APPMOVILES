package com.example.ligortravel.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Documento de experiencias/{destinoId}/resenas/{uid}. El id es el uid del autor: así cada
// turista tiene una sola reseña por destino, que puede editar o borrar.
data class Resena(
    @DocumentId
    val id: String = "",
    val usuarioId: String = "",
    // Nombre público abreviado (ej. "Paul L."), copiado al escribir la reseña: los demás usuarios
    // no pueden leer el perfil completo de otro.
    val nombre: String = "",
    val estrellas: Int = 0,
    val comentario: String = "",
    // Reserva que demuestra que el autor viajó a este destino (las reglas la verifican).
    val reservaId: String = "",
    val creadaEn: Timestamp? = null,
    val actualizadaEn: Timestamp? = null
)
