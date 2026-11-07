package com.example.ligortravel.data

import com.google.firebase.firestore.DocumentId

// Documento de la colección "experiencias" en Firestore. Todos los campos tienen valor por
// defecto porque Firestore necesita un constructor sin argumentos para convertir el documento
// en objeto (toObject/toObjects). Gracias a eso, un documento al que todavía no se le cargaron
// los campos del detalle (descripcion, galeria...) se sigue leyendo sin errores.
data class Experiencia(
    @DocumentId
    val id: String = "",
    val titulo: String = "",
    val ubicacion: String = "",
    val duracion: String = "",
    val rating: Double = 0.0,
    val precioDesde: Int = 0,
    val categoria: String? = null,
    // Nombre del archivo en res/drawable (ej. "laguna_69"), no el R.drawable: ese número lo
    // genera el compilador y puede cambiar entre builds. Ver ImagenesExperiencia.kt.
    val imagen: String? = null,
    // true = aparece como tarjeta grande en el Home; false = en "Experiencias locales".
    val destacada: Boolean = false,
    // Firestore no garantiza el orden de los documentos, así que lo guardamos explícito.
    val orden: Int = 0,

    // --- Campos de la pantalla de detalle ---
    val descripcion: String = "",
    // Fotos extra del destino, con el mismo formato que "imagen" (nombres de res/drawable).
    val galeria: List<String> = emptyList(),
    // Coordenadas para abrir Google Maps. Son null si el destino aún no las tiene cargadas;
    // en ese caso el mapa se busca por el texto de "ubicacion".
    val latitud: Double? = null,
    val longitud: Double? = null,
    // Lo que incluye el tour, ej. ["Transporte", "Guía", "Almuerzo"].
    val incluye: List<String> = emptyList(),
    // Ej. "Mayo a septiembre (temporada seca)".
    val mejorEpoca: String? = null,

    // --- Calificación de los turistas ---
    // Suma de las estrellas y cantidad de reseñas. Se actualizan en la misma transacción que
    // cada reseña (ver ResenaRepository); el promedio es suma / total (ReglasResenas.promedio).
    // "rating" quedó del catálogo inicial (lo escribía el administrador) y ya no se muestra.
    val sumaEstrellas: Int = 0,
    val totalResenas: Int = 0
)
