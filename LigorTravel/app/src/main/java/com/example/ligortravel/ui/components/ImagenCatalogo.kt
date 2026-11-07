package com.example.ligortravel.ui.components

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ligortravel.data.FotoRepository
import com.example.ligortravel.data.idDeFoto
import com.example.ligortravel.ui.home.imagenDeExperiencia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Fotos ya descargadas y decodificadas, para no pedirlas de nuevo a Firestore al volver a una
// pantalla. Guarda las 40 más recientes.
private val fotosEnMemoria = LruCache<String, ImageBitmap>(40)

// Muestra la imagen de un destino o paquete, venga de un drawable local o de una foto subida por
// el administrador. Mientras la foto carga, o si no existe, muestra "alternativa" (el ícono de
// siempre). No envuelve nada en un layout propio: así "alternativa" puede usar Modifier.align
// del Box que la contiene.
@Composable
fun ImagenCatalogo(
    referencia: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alternativa: @Composable () -> Unit
) {
    val drawable = imagenDeExperiencia(referencia)
    val idFoto = idDeFoto(referencia)

    if (drawable != null) {
        Image(
            painter = painterResource(drawable),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else if (idFoto != null) {
        // produceState arranca una corrutina ligada a esta composición: si el usuario sale de la
        // pantalla antes de que termine la descarga, se cancela sola.
        val foto by produceState(initialValue = fotosEnMemoria.get(idFoto), idFoto) {
            if (value == null) value = cargarFoto(idFoto)
        }
        val cargada = foto
        if (cargada != null) {
            Image(
                bitmap = cargada,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        } else {
            alternativa()
        }
    } else {
        alternativa()
    }
}

// Vista previa de una foto recién elegida que todavía no se subió.
@Composable
fun ImagenDeBytes(bytes: ByteArray, contentDescription: String?, modifier: Modifier = Modifier) {
    val foto = remember(bytes) { decodificar(bytes) }
    if (foto != null) {
        Image(
            bitmap = foto,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

private suspend fun cargarFoto(id: String): ImageBitmap? {
    val bytes = FotoRepository().obtener(id) ?: return null
    // Decodificar un JPEG es trabajo de CPU: se hace fuera del hilo principal.
    val foto = withContext(Dispatchers.Default) { decodificar(bytes) } ?: return null
    fotosEnMemoria.put(id, foto)
    return foto
}

private fun decodificar(bytes: ByteArray): ImageBitmap? =
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
