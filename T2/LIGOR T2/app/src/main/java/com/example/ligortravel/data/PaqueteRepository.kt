package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaqueteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    // Siembra temporal: agrega "descripcion" y "serviciosIncluidos" a los paquetes que ya estaban
    // en Firestore antes de que existieran esos campos (sembrarSiEstaVacia no vuelve a escribir
    // si la colección tiene datos). Usa update(), que deja intactos los demás campos.
    // Borrar esta función junto con sembrarSiEstaVacia una vez que los datos estén en Firestore.
    suspend fun completarDetallesSiFaltan(iniciales: List<Paquete>): Result<Boolean> = try {
        // Si el primer paquete ya tiene servicios, la siembra ya corrió: no se vuelve a escribir.
        val servicios = paquetes.document(iniciales.first().id).get().await().get("serviciosIncluidos")
        val yaSembrado = servicios is List<*> && servicios.isNotEmpty()
        if (!yaSembrado) {
            val lote = firestore.batch()
            iniciales.forEach { paquete ->
                lote.update(
                    paquetes.document(paquete.id),
                    mapOf(
                        "descripcion" to paquete.descripcion,
                        "serviciosIncluidos" to paquete.serviciosIncluidos
                    )
                )
            }
            lote.commit().await()
        }
        Result.success(!yaSembrado)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Siembra temporal: sube los paquetes iniciales solo si la colección está vacía.
    // Borrar esta función (y PaquetesIniciales.kt) una vez que los datos estén en Firestore.
    suspend fun sembrarSiEstaVacia(iniciales: List<Paquete>): Result<Boolean> = try {
        val vacia = paquetes.limit(1).get().await().isEmpty
        if (vacia) {
            val lote = firestore.batch()
            iniciales.forEach { paquete ->
                // El id va como nombre del documento; @DocumentId hace que no se guarde también
                // como campo dentro del documento.
                lote.set(paquetes.document(paquete.id), paquete)
            }
            lote.commit().await()
        }
        Result.success(vacia)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
