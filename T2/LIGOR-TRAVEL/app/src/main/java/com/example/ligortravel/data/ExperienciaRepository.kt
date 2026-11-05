package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ExperienciaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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

    // Siembra temporal: agrega los campos del detalle a los documentos existentes.
    // Borrar esta función (y DetallesIniciales.kt) una vez que los datos estén en Firestore.
    // Usa update() y no set(): update solo agrega/reemplaza los campos indicados y deja
    // intactos los demás (titulo, precio, orden...).
    suspend fun completarDetallesSiFaltan(detalles: Map<String, Map<String, Any>>): Result<Boolean> = try {
        // Si el primer destino ya tiene galería, la siembra ya corrió: no se vuelve a escribir.
        // (Se revisa "galeria" y no "descripcion" porque la galería se agregó después: si la
        // primera versión de la siembra ya subió las descripciones, esta igual debe correr.)
        val galeria = experiencias.document(detalles.keys.first()).get().await().get("galeria")
        val yaSembrado = galeria is List<*> && galeria.isNotEmpty()
        if (!yaSembrado) {
            val lote = firestore.batch()
            detalles.forEach { (id, campos) ->
                lote.update(experiencias.document(id), campos)
            }
            lote.commit().await()
        }
        Result.success(!yaSembrado)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
