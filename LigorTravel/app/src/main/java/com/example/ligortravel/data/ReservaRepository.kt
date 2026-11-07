package com.example.ligortravel.data

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Se lanza dentro de la transacción cuando ya no quedan cupos suficientes.
class SinCuposException(val disponibles: Int) : Exception()

// Se lanza si otra persona canceló la reserva mientras se intentaba cancelarla.
class YaCanceladaException : Exception()

class ReservaRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val reservas = firestore.collection("reservas")
    private val paquetes = firestore.collection("paquetes")

    // La reserva y el descuento de cupos se escriben en una sola transacción: Firestore lee los
    // cupos actuales y, si otro usuario los cambia antes de terminar, repite todo con el valor
    // nuevo. Así dos personas no pueden quedarse con el último cupo.
    // El id de la reserva se genera antes (document()) porque el paquete lo guarda en
    // "ultimaReservaId": las reglas lo usan para comprobar que el descuento es de esta reserva.
    // Ese id también sirve como código de la reserva.
    suspend fun crearReserva(reserva: Reserva): Result<String> = try {
        val documentoReserva = reservas.document()
        val documentoPaquete = paquetes.document(reserva.paqueteId)
        firestore.runTransaction { transaccion ->
            val cupos = transaccion.get(documentoPaquete).getLong("cupos")?.toInt() ?: 0
            if (cupos < reserva.personas) throw SinCuposException(cupos)
            // Un mapa y no el objeto Reserva: el objeto también escribiría los campos de pago y
            // cancelación en null, y las reglas solo aceptan estos nueve al crear.
            transaccion.set(
                documentoReserva,
                mapOf(
                    "usuarioId" to reserva.usuarioId,
                    "paqueteId" to reserva.paqueteId,
                    "paqueteTitulo" to reserva.paqueteTitulo,
                    "fechaViaje" to reserva.fechaViaje,
                    "personas" to reserva.personas,
                    "precioUnitario" to reserva.precioUnitario,
                    "total" to reserva.total,
                    "estado" to reserva.estado,
                    "creadaEn" to FieldValue.serverTimestamp()
                )
            )
            transaccion.update(
                documentoPaquete,
                mapOf(
                    "cupos" to cupos - reserva.personas,
                    "ultimaReservaId" to documentoReserva.id
                )
            )
        }.await()
        Result.success(documentoReserva.id)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeErrorReserva(e)))
    }

    // Cancela y devuelve los cupos al paquete en la misma transacción (el camino inverso de
    // crearReserva). Si el paquete ya fue eliminado, solo se cancela la reserva.
    // Las reglas deciden quién puede cancelar: el turista, solo las suyas pendientes de pago y
    // hasta 48 horas antes; el administrador, cualquiera que no esté cancelada.
    suspend fun cancelarReserva(reservaId: String, canceladaPor: String, motivo: String?): Result<Unit> = try {
        val documentoReserva = reservas.document(reservaId)
        firestore.runTransaction { transaccion ->
            // En una transacción todas las lecturas van antes que las escrituras.
            val reserva = transaccion.get(documentoReserva)
            if (reserva.getString("estado") == Reserva.ESTADO_CANCELADA) throw YaCanceladaException()
            val personas = reserva.getLong("personas") ?: 0L
            val documentoPaquete = reserva.getString("paqueteId")
                ?.takeIf { it.isNotBlank() }
                ?.let { paquetes.document(it) }
            val paquete = documentoPaquete?.let { transaccion.get(it) }

            val cambios = mutableMapOf<String, Any>(
                "estado" to Reserva.ESTADO_CANCELADA,
                "canceladaEn" to FieldValue.serverTimestamp(),
                "canceladaPor" to canceladaPor
            )
            motivo?.trim()?.takeIf { it.isNotEmpty() }?.let { cambios["motivoCancelacion"] = it }
            transaccion.update(documentoReserva, cambios)

            if (documentoPaquete != null && paquete?.exists() == true) {
                val cupos = paquete.getLong("cupos") ?: 0L
                transaccion.update(
                    documentoPaquete,
                    mapOf("cupos" to cupos + personas, "ultimaReservaId" to reservaId)
                )
            }
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeErrorCancelacion(e)))
    }

    // Cobro simulado: solo cambia el estado y guarda cómo se pagó. Las reglas exigen que sea el
    // administrador y que la reserva siga pendiente de pago.
    suspend fun registrarPago(reservaId: String, metodoPago: String): Result<Unit> = try {
        reservas.document(reservaId).update(
            mapOf(
                "estado" to Reserva.ESTADO_PAGADA,
                "pagadaEn" to FieldValue.serverTimestamp(),
                "metodoPago" to metodoPago
            )
        ).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeErrorGenerico(e, "No se pudo registrar el pago: la reserva ya no está pendiente")))
    }

    // Reservas del usuario en tiempo real (si se crea una nueva, el historial se actualiza solo).
    // Solo filtra por usuarioId, sin orderBy: combinar un where sobre un campo con un orderBy
    // sobre otro obliga a crear un índice compuesto en la consola. Como un usuario tiene pocas
    // reservas, se ordenan en memoria (ver separarReservas).
    // Además, el filtro es obligatorio: las reglas de Firestore NO filtran resultados, sino que
    // rechazan la consulta completa si podría devolver documentos que el usuario no puede leer.
    // Al pedir solo "usuarioId == mi uid", la consulta cumple la regla de lectura.
    fun observarReservasDeUsuario(usuarioId: String): Flow<Result<List<Reserva>>> =
        observar(reservas.whereEqualTo("usuarioId", usuarioId))

    // Todas las reservas: solo funciona para el administrador (las reglas rechazan a los demás).
    fun observarTodasLasReservas(): Flow<Result<List<Reserva>>> = observar(reservas)

    private fun observar(consulta: com.google.firebase.firestore.Query): Flow<Result<List<Reserva>>> = callbackFlow {
        val registro = consulta.addSnapshotListener { snapshot, error ->
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
    // La SinCuposException puede llegar envuelta por Firestore, así que se busca en las causas.
    private fun mensajeErrorReserva(e: Exception): String {
        val sinCupos = buscarCausa<SinCuposException>(e)
        if (sinCupos != null) {
            return if (sinCupos.disponibles == 0) {
                "Este paquete se agotó mientras reservabas"
            } else {
                "Solo quedan ${sinCupos.disponibles} cupos, reduce el número de personas"
            }
        }
        // PERMISSION_DENIED: las reglas rechazaron la reserva. Por ejemplo, el precio del paquete
        // cambió mientras el usuario estaba en la pantalla, o la fecha/personas no son válidas.
        return mensajeErrorGenerico(
            e,
            "No se pudo confirmar la reserva: los datos no son válidos o el precio cambió. Revisa e intenta de nuevo"
        )
    }

    private fun mensajeErrorCancelacion(e: Exception): String {
        if (buscarCausa<YaCanceladaException>(e) != null) return "Esta reserva ya estaba cancelada"
        return mensajeErrorGenerico(
            e,
            "No se pudo cancelar: la reserva ya no cumple las condiciones de cancelación"
        )
    }

    private fun mensajeErrorGenerico(e: Exception, permisoDenegado: String): String =
        when (buscarCausa<FirebaseFirestoreException>(e)?.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> permisoDenegado
            FirebaseFirestoreException.Code.UNAVAILABLE -> "No hay conexión a internet, verifica tu red"
            else -> "No se pudo completar la operación, intenta de nuevo"
        }

    private inline fun <reified T : Throwable> buscarCausa(e: Throwable): T? =
        generateSequence(e) { it.cause }.filterIsInstance<T>().firstOrNull()
}
