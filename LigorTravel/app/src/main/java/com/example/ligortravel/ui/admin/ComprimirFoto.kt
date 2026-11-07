package com.example.ligortravel.ui.admin

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import com.example.ligortravel.data.MAX_BYTES_FOTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

// Reduce la foto elegida y la convierte a JPEG hasta que pese menos de MAX_BYTES_FOTO, para que
// quepa en un documento de Firestore (máximo 1 MiB). Devuelve null si no se pudo leer o si ni
// con la calidad más baja queda bajo el límite.
suspend fun comprimirFoto(resolver: ContentResolver, uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
    try {
        val origen = ImageDecoder.createSource(resolver, uri)
        // ImageDecoder ya aplica la rotación guardada en la foto (EXIF), así que no sale de lado.
        val bitmap = ImageDecoder.decodeBitmap(origen) { decoder, info, _ ->
            val (ancho, alto) = ReglasAdmin.dimensionesReducidas(info.size.width, info.size.height)
            decoder.setTargetSize(ancho, alto)
        }
        var calidad = 85
        var bytes: ByteArray
        do {
            bytes = ByteArrayOutputStream().use { salida ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
                salida.toByteArray()
            }
            calidad -= 10
        } while (bytes.size > MAX_BYTES_FOTO && calidad >= 35)
        bytes.takeIf { it.size <= MAX_BYTES_FOTO }
    } catch (e: Exception) {
        null
    }
}
