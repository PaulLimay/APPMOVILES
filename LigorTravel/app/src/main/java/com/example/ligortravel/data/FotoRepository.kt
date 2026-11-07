package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Las fotos subidas por el administrador se guardan en la colección "fotos", una por documento,
// porque Firebase Storage exige el plan Blaze. Cada documento tiene un solo campo "datos" con los
// bytes del JPEG (tipo Blob). Firestore limita cada documento a 1 MiB: por eso la app comprime
// las fotos antes de subirlas.
//
// Los campos "imagen" y "galeria" de Experiencia y Paquete guardan un texto. Puede ser el nombre
// de un drawable local (los destinos originales, ej. "laguna_69") o una foto subida con el
// formato "foto:<id del documento>".

const val PREFIJO_FOTO_SUBIDA = "foto:"
const val MAX_BYTES_FOTO = 700_000

fun referenciaDeFoto(id: String): String = PREFIJO_FOTO_SUBIDA + id

// Devuelve el id del documento si la referencia es una foto subida, o null si es un drawable.
fun idDeFoto(referencia: String?): String? =
    referencia
        ?.takeIf { it.startsWith(PREFIJO_FOTO_SUBIDA) }
        ?.removePrefix(PREFIJO_FOTO_SUBIDA)
        ?.takeIf { it.isNotBlank() }

class FotoRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val fotos = firestore.collection("fotos")

    // document() sin argumentos genera un id nuevo en el celular, sin ir a la red. Así la app
    // conoce el id antes de guardar y puede escribir la referencia en el destino en el mismo lote.
    fun nuevoId(): String = fotos.document().id

    suspend fun obtener(id: String): ByteArray? = try {
        fotos.document(id).get().await().getBlob("datos")?.toBytes()
    } catch (e: Exception) {
        null
    }
}
