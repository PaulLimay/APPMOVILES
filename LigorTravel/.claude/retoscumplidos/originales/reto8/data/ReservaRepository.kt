package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReservaRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val reservas = firestore.collection("reservas")

    // add() crea un documento con un id aleatorio generado por Firestore y lo devuelve: ese id
    // sirve como código de la reserva.
    suspend fun crearReserva(reserva: Reserva): Result<String> = try {
        val documento = reservas.add(reserva).await()
        Result.success(documento.id)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    // Reservas del usuario en tiempo real (si se crea una nueva, el historial se actualiza solo).
    // Solo filtra por usuarioId, sin orderBy: combinar un where sobre un campo con un orderBy
    // sobre otro obliga a crear un índice compuesto en la consola. Como un usuario tiene pocas
    // reservas, se ordenan en memoria (ver separarReservas).
    // Además, el filtro es obligatorio: las reglas de Firestore NO filtran resultados, sino que
    // rechazan la consulta completa si podría devolver documentos que el usuario no puede leer.
    // Al pedir solo "usuarioId == mi uid", la consulta cumple la regla de lectura.
    fun observarReservasDeUsuario(usuarioId: String): Flow<Result<List<Reserva>>> = callbackFlow {
        val registro = reservas.whereEqualTo("usuarioId", usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(Result.success(snapshot.toObjects(Reserva::class.java)))
                }
            }
        awaitClose { registro.remove() }
    }

    // Igual que en AuthRepository: traducimos los errores de Firebase a mensajes para el usuario.
    private fun mensajeError(e: Exception): String = when ((e as? FirebaseFirestoreException)?.code) {
        // Las reglas del servidor rechazaron la reserva: por ejemplo, el precio del paquete cambió
        // mientras el usuario estaba en la pantalla, o la fecha/personas no son válidas.
        FirebaseFirestoreException.Code.PERMISSION_DENIED ->
            "No se pudo confirmar la reserva: los datos no son válidos o el precio cambió. Revisa e intenta de nuevo"
        FirebaseFirestoreException.Code.UNAVAILABLE ->
            "No hay conexión a internet, verifica tu red"
        else -> "No se pudo registrar la reserva, intenta de nuevo"
    }
}
