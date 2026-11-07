package com.example.ligortravel.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.GenerateContentResponse
import com.google.firebase.ai.type.PromptBlockedException
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ResponseStoppedException
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.ServerException
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

// Se prueban en orden. En el plan gratuito, Google puede rechazar pedidos cuando un modelo está
// saturado ("high demand"); entonces se reintenta con el siguiente, que tiene su propia cuota.
// Ambos funcionan sin facturación (plan Spark).
private val MODELOS = listOf("gemini-3.8-flash", "gemini-3.5-flash-lite")
private const val ESPERA_ANTES_DE_REINTENTAR_MS = 800L

// Habla con Gemini mediante Firebase AI Logic. La clave de Gemini no está en la app: la guarda
// Firebase en su servidor, y App Check le demuestra que la llamada viene de esta app.
class ChatRepository(
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository(),
    private val paqueteRepository: PaqueteRepository = PaqueteRepository()
) {

    // Structured outputs: obliga al modelo a responder con este JSON en vez de texto libre.
    private val esquemaRespuesta = Schema.obj(
        mapOf(
            "texto" to Schema.string(),
            "destinoIds" to Schema.array(Schema.string()),
            "paqueteIds" to Schema.array(Schema.string())
        )
    )

    suspend fun enviar(mensajes: List<MensajeChat>): Result<RespuestaChat> = try {
        // first() toma el catálogo actual y cierra el listener: aquí no hace falta tiempo real.
        val destinos = experienciaRepository.observarExperiencias().first().getOrThrow()
        val paquetes = paqueteRepository.observarPaquetes().first().getOrThrow()
        val instrucciones = construirInstrucciones(construirCatalogo(destinos, paquetes))

        val respuesta = enviarConRespaldo(instrucciones, historialParaEnviar(mensajes))

        val resultado = armarRespuesta(respuesta.text, destinos, paquetes)
        if (resultado != null) {
            Result.success(resultado)
        } else {
            Result.failure(Exception("Ligor no pudo responder, intenta de nuevo"))
        }
    } catch (e: Exception) {
        Log.e("ChatRepository", "Falló la llamada a Gemini", e)
        Result.failure(Exception(mensajeError(e)))
    }

    // Solo se pasa al siguiente modelo si el problema es de capacidad (servidor saturado o cuota
    // agotada). Otros errores, como una pregunta bloqueada, se devuelven de inmediato.
    private suspend fun enviarConRespaldo(
        instrucciones: String,
        historial: List<MensajeChat>
    ): GenerateContentResponse {
        var ultimoError: Exception? = null
        for ((indice, nombreModelo) in MODELOS.withIndex()) {
            if (indice > 0) delay(ESPERA_ANTES_DE_REINTENTAR_MS)
            try {
                val chat = crearModelo(nombreModelo, instrucciones).startChat(historial.dropLast(1).map(::aContent))
                return chat.sendMessage(historial.last().texto)
            } catch (e: ServerException) {
                Log.w("ChatRepository", "$nombreModelo no disponible, probando el siguiente", e)
                ultimoError = e
            } catch (e: QuotaExceededException) {
                Log.w("ChatRepository", "$nombreModelo sin cuota, probando el siguiente", e)
                ultimoError = e
            }
        }
        throw ultimoError ?: IllegalStateException("No hay modelos configurados")
    }

    private fun crearModelo(nombreModelo: String, instrucciones: String): GenerativeModel =
        Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel(
            modelName = nombreModelo,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = esquemaRespuesta
            },
            systemInstruction = content { text(instrucciones) }
        )

    private fun aContent(mensaje: MensajeChat): Content =
        content(role = if (mensaje.esDelUsuario) "user" else "model") { text(mensaje.texto) }

    private fun mensajeError(e: Exception): String = when (e) {
        is ServerException -> "Ligor está recibiendo muchas consultas en este momento, intenta en unos minutos"
        is QuotaExceededException -> "Ligor recibió muchos mensajes, intenta más tarde"
        is PromptBlockedException, is ResponseStoppedException ->
            "Ligor no puede responder a eso, prueba con otra pregunta"
        else -> "Ligor no pudo responder, verifica tu conexión e intenta de nuevo"
    }
}
