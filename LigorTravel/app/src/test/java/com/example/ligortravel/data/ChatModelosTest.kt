package com.example.ligortravel.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatModelosTest {

    private val destinos = listOf("1", "2", "3").map { id ->
        Experiencia(id = id, titulo = "Destino $id", ubicacion = "Ancash", precioDesde = 120, imagen = "laguna_69")
    }
    private val paquetes = listOf("a", "b").map { id ->
        Paquete(id = id, titulo = "Paquete $id", destino = "Cusco", duracion = "5 días", precio = 899)
    }

    private fun armar(json: String?) = armarRespuesta(json, destinos, paquetes)

    @Test
    fun `una respuesta sin recomendaciones es valida`() {
        val respuesta = armar("""{"texto":"¿Cuántos días tienes?","destinoIds":[],"paqueteIds":[]}""")

        assertEquals("¿Cuántos días tienes?", respuesta?.texto)
        assertTrue(respuesta?.recomendaciones.orEmpty().isEmpty())
    }

    @Test
    fun `descarta los ids que no existen en el catalogo`() {
        val respuesta = armar("""{"texto":"Hola","destinoIds":["1","inventado"],"paqueteIds":["otro"]}""")

        assertEquals(listOf("1"), respuesta?.recomendaciones?.map { it.id })
    }

    @Test
    fun `toma el titulo y el precio de Firestore`() {
        val respuesta = armar("""{"texto":"Solo cuesta S/ 1","destinoIds":[],"paqueteIds":["a"]}""")

        val recomendacion = respuesta?.recomendaciones?.single()
        assertEquals(TipoRecomendacion.PAQUETE, recomendacion?.tipo)
        assertEquals("Paquete a", recomendacion?.titulo)
        assertEquals(899, recomendacion?.precio)
    }

    @Test
    fun `quita repetidos, pone primero los paquetes y limita las tarjetas`() {
        val respuesta = armar("""{"texto":"Hola","destinoIds":["1","1","2","3"],"paqueteIds":["a","b"]}""")

        assertEquals(listOf("a", "b", "1"), respuesta?.recomendaciones?.map { it.id })
    }

    @Test
    fun `tolera que falten las listas de ids`() {
        assertTrue(armar("""{"texto":"Hola"}""")?.recomendaciones.orEmpty().isEmpty())
    }

    @Test
    fun `devuelve null si la respuesta no se puede usar`() {
        assertNull(armar(null))
        assertNull(armar("esto no es JSON"))
        assertNull(armar("""{"texto":"   "}"""))
    }

    @Test
    fun `el catalogo incluye los datos utiles pero no las imagenes`() {
        val catalogo = construirCatalogo(destinos, paquetes)

        assertTrue(catalogo.contains("Destino 1"))
        assertTrue(catalogo.contains("899"))
        assertFalse(catalogo.contains("laguna_69"))
    }

    @Test
    fun `las instrucciones incluyen el catalogo completo`() {
        assertTrue(construirInstrucciones("""{"destinos":[]}""").endsWith("""{"destinos":[]}"""))
    }

    @Test
    fun `el historial conserva solo los ultimos mensajes`() {
        val mensajes = List(MAX_MENSAJES_HISTORIAL + 4) { i ->
            MensajeChat("Mensaje $i", esDelUsuario = true)
        }

        val historial = historialParaEnviar(mensajes)

        assertEquals(MAX_MENSAJES_HISTORIAL, historial.size)
        assertEquals("Mensaje 4", historial.first().texto)
    }

    @Test
    fun `el historial siempre empieza con un mensaje del usuario`() {
        val mensajes = List(MAX_MENSAJES_HISTORIAL + 1) { i ->
            MensajeChat("Mensaje $i", esDelUsuario = i % 2 == 0)
        }

        assertTrue(historialParaEnviar(mensajes).first().esDelUsuario)
    }
}
