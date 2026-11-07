package com.example.ligortravel.ui.admin

import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.idDeFoto
import com.example.ligortravel.ui.mapa.ReglasMapa

// Reglas del panel de administración, sin Firebase ni Android para poder probarlas con tests JVM
// (mismo criterio que ReglasReserva). Si cambias un límite, cámbialo también en firestore.rules.
object ReglasAdmin {
    const val TITULO_MIN = 3
    const val TITULO_MAX = 80
    const val PRECIO_MAX = 100_000
    const val CUPOS_MAX = 1000
    const val DESCRIPCION_MAX = 1000
    const val MAX_FOTOS_GALERIA = 3
    const val LADO_MAXIMO_FOTO = 1080

    fun validarDestino(formulario: FormularioDestino, tieneFoto: Boolean): String? {
        return when {
            !tieneFoto -> "Agrega la foto principal del destino"
            formulario.titulo.trim().length !in TITULO_MIN..TITULO_MAX ->
                "El nombre debe tener entre $TITULO_MIN y $TITULO_MAX caracteres"
            formulario.ubicacion.isBlank() -> "Escribe la ubicación"
            formulario.duracion.isBlank() -> "Escribe la duración"
            formulario.precioDesde.toIntOrNull() !in 1..PRECIO_MAX ->
                "El precio debe ser un número entero entre 1 y $PRECIO_MAX"
            formulario.descripcion.length > DESCRIPCION_MAX ->
                "La descripción no puede pasar de $DESCRIPCION_MAX caracteres"
            else -> validarCoordenadas(formulario.latitud, formulario.longitud)
        }
    }

    // Las coordenadas son opcionales, pero van juntas: sin ellas el destino no aparece en el mapa.
    fun validarCoordenadas(latitud: String, longitud: String): String? {
        if (latitud.isBlank() && longitud.isBlank()) return null
        val lat = numero(latitud)
        val lon = numero(longitud)
        return when {
            lat == null || lon == null -> "Escribe la latitud y la longitud, o deja ambas vacías"
            !ReglasMapa.esCoordenadaValida(lat, lon) ->
                "Coordenadas fuera de rango: latitud entre -90 y 90, longitud entre -180 y 180"
            else -> null
        }
    }

    // Acepta coma o punto decimal, como se escriben en un celular en español.
    fun numero(texto: String): Double? = texto.trim().replace(',', '.').toDoubleOrNull()

    fun validarPaquete(formulario: FormularioPaquete, tieneFoto: Boolean): String? = when {
        !tieneFoto -> "Agrega la foto de portada del paquete"
        formulario.titulo.trim().length !in TITULO_MIN..TITULO_MAX ->
            "El nombre debe tener entre $TITULO_MIN y $TITULO_MAX caracteres"
        formulario.destino.isBlank() -> "Escribe la zona o destino del paquete"
        formulario.duracion.isBlank() -> "Escribe la duración"
        formulario.precio.toIntOrNull() !in 1..PRECIO_MAX ->
            "El precio debe ser un número entero entre 1 y $PRECIO_MAX"
        formulario.cupos.toIntOrNull() !in 0..CUPOS_MAX ->
            "Los cupos deben ser un número entero entre 0 y $CUPOS_MAX"
        formulario.experienciaIds.isEmpty() -> "Elige al menos un destino para el paquete"
        formulario.descripcion.length > DESCRIPCION_MAX ->
            "La descripción no puede pasar de $DESCRIPCION_MAX caracteres"
        else -> null
    }

    // Los campos de lista (incluye, servicios) se escriben uno por línea.
    fun lineas(texto: String): List<String> = texto.lines().map { it.trim() }.filter { it.isNotEmpty() }

    // Un destino no se puede eliminar si algún paquete lo incluye: el paquete quedaría con una
    // actividad que ya no existe.
    fun paquetesQueIncluyen(destinoId: String, paquetes: List<Paquete>): List<Paquete> =
        paquetes.filter { destinoId in it.experienciaIds }

    // Los elementos nuevos van al final de la lista.
    fun siguienteOrden(ordenes: List<Int>): Int = (ordenes.maxOrNull() ?: 0) + 1

    // Reduce la foto para que su lado más largo mida como máximo ladoMaximo, sin deformarla.
    // Una foto más pequeña se deja igual.
    fun dimensionesReducidas(ancho: Int, alto: Int, ladoMaximo: Int = LADO_MAXIMO_FOTO): Pair<Int, Int> {
        val mayor = maxOf(ancho, alto)
        if (mayor <= ladoMaximo) return ancho to alto
        val escala = ladoMaximo.toDouble() / mayor
        return maxOf(1, (ancho * escala).toInt()) to maxOf(1, (alto * escala).toInt())
    }

    // Compara las fotos guardadas con las del formulario: las nuevas hay que subirlas y las que
    // el administrador quitó hay que borrarlas de la colección "fotos".
    fun cambiosDeFotos(originales: List<String>, actuales: List<FotoFormulario>): CambiosFotos {
        val nuevas = actuales.mapNotNull { foto ->
            val id = idDeFoto(foto.referencia)
            val bytes = foto.bytesNuevos
            if (id != null && bytes != null) id to bytes else null
        }.toMap()
        val referenciasActuales = actuales.map { it.referencia }.toSet()
        val eliminadas = originales.filter { it !in referenciasActuales }.mapNotNull(::idDeFoto)
        return CambiosFotos(nuevas, eliminadas)
    }
}

// Los números se guardan como texto mientras se escriben: así el campo puede quedar vacío y el
// error se muestra al guardar, no con cada tecla.
data class FormularioDestino(
    val titulo: String = "",
    val ubicacion: String = "",
    val categoria: String = "",
    val duracion: String = "",
    val precioDesde: String = "",
    val descripcion: String = "",
    val incluye: String = "",
    val mejorEpoca: String = "",
    val destacada: Boolean = false,
    val latitud: String = "",
    val longitud: String = ""
)

data class FormularioPaquete(
    val titulo: String = "",
    val destino: String = "",
    val duracion: String = "",
    val precio: String = "",
    val cupos: String = "",
    val descripcion: String = "",
    val serviciosIncluidos: String = "",
    val experienciaIds: List<String> = emptyList()
)

// Una foto del formulario: una ya guardada (solo referencia) o una recién elegida, que además
// trae sus bytes para subirla al guardar.
data class FotoFormulario(val referencia: String, val bytesNuevos: ByteArray? = null)

data class CambiosFotos(val nuevas: Map<String, ByteArray>, val eliminadas: List<String>)
