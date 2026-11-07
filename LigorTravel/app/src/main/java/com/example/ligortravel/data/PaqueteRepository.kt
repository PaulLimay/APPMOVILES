package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class PaqueteRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val paquetes = firestore.collection("paquetes")

    // Escucha la colección en tiempo real, igual que ExperienciaRepository.observarExperiencias().
    fun observarPaquetes(): Flow<Result<List<Paquete>>> = callbackFlow {
        val registro = paquetes.orderBy("orden").addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(Result.success(snapshot.toObjects(Paquete::class.java)))
            }
        }
        awaitClose { registro.remove() }
    }

    // Igual que observarPaquetes(), pero escucha un solo documento: el del paquete que se está
    // viendo en la pantalla de detalle del paquete.
    fun observarPaquete(id: String): Flow<Result<Paquete>> = callbackFlow {
        val registro = paquetes.document(id).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            // toObject devuelve null si el documento no existe (por ejemplo, si lo borraron
            // desde la consola mientras el usuario lo estaba viendo).
            val paquete = snapshot?.toObject(Paquete::class.java)
            if (paquete != null) {
                trySend(Result.success(paquete))
            } else {
                trySend(Result.failure(NoSuchElementException("Paquete no encontrado: $id")))
            }
        }
        awaitClose { registro.remove() }
    }
}
