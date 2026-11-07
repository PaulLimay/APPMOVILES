package com.example.ligortravel.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ResenaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val experiencias = firestore.collection("experiencias")

    // Subcolección: las reseñas viven "dentro" de su destino.
    private fun resenas(destinoId: String) = experiencias.document(destinoId).collection("resenas")

    // Tiempo real: si otro turista publica una reseña, aparece sola en la ficha.
    fun observarResenas(destinoId: String): Flow<Result<List<Resena>>> = callbackFlow {
        val registro = resenas(destinoId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Result.success(snapshot.toObjects(Resena::class.java)))
            }
        }
        awaitClose { registro.remove() }
    }

    // Crea o edita la reseña del usuario y ajusta la suma y el total del destino en la misma
    // transacción (igual que los cupos al reservar). Si es una edición, solo cambia la diferencia
    // de estrellas; el total se mantiene.
    suspend fun guardarResena(
        destinoId: String,
        usuarioId: String,
        nombre: String,
        estrellas: Int,
        comentario: String,
        reservaId: String
    ): Result<Unit> = try {
        val documentoDestino = experiencias.document(destinoId)
        val documentoResena = resenas(destinoId).document(usuarioId)
        firestore.runTransaction { transaccion ->
            val destino = transaccion.get(documentoDestino)
            val anterior = transaccion.get(documentoResena)
            val suma = destino.getLong("sumaEstrellas") ?: 0L
            val total = destino.getLong("totalResenas") ?: 0L
            val campos = mapOf(
                "usuarioId" to usuarioId,
                "nombre" to nombre,
                "estrellas" to estrellas,
                "comentario" to comentario,
                "reservaId" to reservaId
            )
            if (anterior.exists()) {
                val estrellasAntes = anterior.getLong("estrellas") ?: 0L
                // update conserva "creadaEn"; las reglas exigen que no cambie.
                transaccion.update(documentoResena, campos + ("actualizadaEn" to FieldValue.serverTimestamp()))
                transaccion.update(documentoDestino, "sumaEstrellas", suma - estrellasAntes + estrellas)
            } else {
                transaccion.set(documentoResena, campos + ("creadaEn" to FieldValue.serverTimestamp()))
                transaccion.update(
                    documentoDestino,
                    mapOf("sumaEstrellas" to suma + estrellas, "totalResenas" to total + 1)
                )
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e, "No se pudo publicar: solo puedes reseñar destinos que ya visitaste")))
    }

    // La borra su autor o el administrador (moderación). También resta sus estrellas del destino.
    suspend fun eliminarResena(destinoId: String, autorId: String): Result<Unit> = try {
        val documentoDestino = experiencias.document(destinoId)
        val documentoResena = resenas(destinoId).document(autorId)
        firestore.runTransaction { transaccion ->
            val destino = transaccion.get(documentoDestino)
            val resena = transaccion.get(documentoResena)
            if (resena.exists()) {
                val estrellas = resena.getLong("estrellas") ?: 0L
                transaccion.delete(documentoResena)
                transaccion.update(
                    documentoDestino,
                    mapOf(
                        "sumaEstrellas" to (destino.getLong("sumaEstrellas") ?: 0L) - estrellas,
                        "totalResenas" to (destino.getLong("totalResenas") ?: 0L) - 1
                    )
                )
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e, "No tienes permiso para borrar esta reseña")))
    }

    private fun mensajeError(e: Exception, permisoDenegado: String): String =
        when (generateSequence<Throwable>(e) { it.cause }.filterIsInstance<FirebaseFirestoreException>().firstOrNull()?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> permisoDenegado
            FirebaseFirestoreException.Code.UNAVAILABLE -> "No hay conexión a internet, verifica tu red"
            else -> "No se pudo completar la operación, intenta de nuevo"
        }
}
