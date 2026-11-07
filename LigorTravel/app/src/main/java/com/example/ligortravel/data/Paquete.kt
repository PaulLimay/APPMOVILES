package com.example.ligortravel.data

import com.google.firebase.firestore.DocumentId

// Documento de la colección "paquetes" en Firestore: un paquete turístico que agrupa varias
// experiencias. Igual que Experiencia, todos los campos tienen valor por defecto para que
// Firestore pueda convertir el documento en objeto (toObjects).
data class Paquete(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    // Zona del paquete, ej. "Cusco" o "Paracas e Ica".
    val destino: String = "",
    // Ej. "5 días / 4 noches".
    val duracion: String = "",
    // Precio total del paquete por persona, en soles.
    val precio: Int = 0,
    // Nombre del drawable de portada (mismo formato que Experiencia.imagen).
    val imagen: String? = null,
    // Ids de documentos de la colección "experiencias" que incluye el paquete. Se guardan solo
    // los ids (referencias) y no una copia de cada experiencia: así, si una experiencia cambia
    // en Firestore, todos los paquetes que la incluyen muestran el dato nuevo.
    val experienciaIds: List<String> = emptyList(),
    val orden: Int = 0,

    // --- Campos de la pantalla de detalle del paquete ---
    val descripcion: String = "",
    // Servicios propios del paquete (hotel, traslados, asistencia...), aparte de lo que ya
    // incluye cada experiencia por separado.
    val serviciosIncluidos: List<String> = emptyList()
)
