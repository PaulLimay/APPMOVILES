package com.example.ligortravel.data

import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Escrituras del panel de administración. La app oculta el panel a quien no es administrador,
// pero la protección real está en las reglas de Firestore (ver firestore.rules): solo aceptan
// estas escrituras si existe el documento admins/{uid} del usuario autenticado.
class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val admins = firestore.collection("admins")
    private val experiencias = firestore.collection("experiencias")
    private val paquetes = firestore.collection("paquetes")
    private val fotos = firestore.collection("fotos")

    // El rol no se guarda en "usuarios" porque cada usuario puede editar su propio perfil y podría
    // nombrarse administrador. Los documentos de "admins" solo se crean desde la consola.
    suspend fun esAdmin(uid: String): Result<Boolean> = try {
        Result.success(admins.document(uid).get().await().exists())
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    // Nombre y correo de los turistas, para que el administrador sepa a quién cobrar. Las reglas
    // solo permiten leer todos los perfiles al administrador.
    suspend fun obtenerUsuarios(): Result<Map<String, RegistroUsuario>> = try {
        val usuarios = firestore.collection("usuarios").get().await().toObjects(RegistroUsuario::class.java)
        Result.success(usuarios.mapNotNull { usuario -> usuario.id?.let { it to usuario } }.toMap())
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    // id null = destino nuevo (Firestore genera el id).
    suspend fun guardarDestino(
        id: String?,
        campos: Map<String, Any?>,
        fotosNuevas: Map<String, ByteArray>,
        fotosEliminadas: List<String>
    ): Result<Unit> = guardar(experiencias, id, campos, fotosNuevas, fotosEliminadas)

    suspend fun guardarPaquete(
        id: String?,
        campos: Map<String, Any?>,
        fotosNuevas: Map<String, ByteArray>,
        fotosEliminadas: List<String>
    ): Result<Unit> = guardar(paquetes, id, campos, fotosNuevas, fotosEliminadas)

    suspend fun eliminarDestino(destino: Experiencia): Result<Unit> =
        eliminar(experiencias.document(destino.id), listOfNotNull(destino.imagen) + destino.galeria)

    // Las reservas de este paquete no se tocan: guardan su propia copia del título y el precio.
    suspend fun eliminarPaquete(paquete: Paquete): Result<Unit> =
        eliminar(paquetes.document(paquete.id), listOfNotNull(paquete.imagen))

    // Un lote (WriteBatch) se aplica completo o no se aplica: nunca queda un destino apuntando a
    // una foto que no se subió, ni fotos sueltas de un destino que no se guardó.
    private suspend fun guardar(
        coleccion: CollectionReference,
        id: String?,
        campos: Map<String, Any?>,
        fotosNuevas: Map<String, ByteArray>,
        fotosEliminadas: List<String>
    ): Result<Unit> = try {
        val lote = firestore.batch()
        fotosNuevas.forEach { (idFoto, bytes) ->
            lote.set(fotos.document(idFoto), mapOf("datos" to Blob.fromBytes(bytes)))
        }
        fotosEliminadas.forEach { idFoto -> lote.delete(fotos.document(idFoto)) }
        val documento = if (id == null) coleccion.document() else coleccion.document(id)
        // merge conserva los campos que el formulario no edita (coordenadas, cupos en uso...).
        lote.set(documento, campos, SetOptions.merge())
        lote.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    private suspend fun eliminar(documento: DocumentReference, referencias: List<String>): Result<Unit> = try {
        val lote = firestore.batch()
        lote.delete(documento)
        referencias.mapNotNull(::idDeFoto).forEach { idFoto -> lote.delete(fotos.document(idFoto)) }
        lote.commit().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    private fun mensajeError(e: Exception): String = when ((e as? FirebaseFirestoreException)?.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "No tienes permiso o los datos no son válidos para el servidor"
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "No hay conexión a internet, verifica tu red"
        else -> "No se pudo completar la operación, intenta de nuevo"
    }
}
