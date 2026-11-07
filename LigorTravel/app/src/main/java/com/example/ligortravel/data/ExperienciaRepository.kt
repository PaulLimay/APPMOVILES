package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class ExperienciaRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val experiencias = firestore.collection("experiencias")

    // Escucha la colección en tiempo real: emite la lista completa al suscribirse y otra vez
    // cada vez que un documento cambia en Firestore (por ejemplo, desde la consola).
    fun observarExperiencias(): Flow<Result<List<Experiencia>>> = callbackFlow {
        val registro = experiencias.orderBy("orden").addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Result.success(snapshot.toObjects(Experiencia::class.java)))
            }
        }
        // Cuando nadie escucha más (el ViewModel se destruye), se quita el listener de Firestore.
        awaitClose { registro.remove() }
    }

    // Igual que observarExperiencias(), pero escucha un solo documento: el del destino que se
    // está viendo en la pantalla de detalle.
    fun observarExperiencia(id: String): Flow<Result<Experiencia>> = callbackFlow {
        val registro = experiencias.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            // toObject devuelve null si el documento no existe (por ejemplo, si lo borraron
            // desde la consola mientras el usuario lo estaba viendo).
            val experiencia = snapshot?.toObject(Experiencia::class.java)
            if (experiencia != null) {
                trySend(Result.success(experiencia))
            } else {
                trySend(Result.failure(NoSuchElementException("Destino no encontrado: $id")))
            }
        }
        awaitClose { registro.remove() }
    }
}
