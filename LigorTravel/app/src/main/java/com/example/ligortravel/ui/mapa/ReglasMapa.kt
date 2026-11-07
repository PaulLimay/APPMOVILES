package com.example.ligortravel.ui.mapa

import com.example.ligortravel.data.Experiencia
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

// Lógica pura del mapa (sin Android ni MapLibre), probada en ReglasMapaTest.

data class Coordenada(val latitud: Double, val longitud: Double)

object ReglasMapa {
    private const val RADIO_TIERRA_KM = 6371.0

    fun esCoordenadaValida(latitud: Double?, longitud: Double?): Boolean =
        latitud != null && longitud != null &&
            latitud in -90.0..90.0 && longitud in -180.0..180.0 &&
            // (0, 0) está en el océano Atlántico: casi siempre es un dato sin cargar.
            !(latitud == 0.0 && longitud == 0.0)

    // Solo los destinos con coordenadas válidas pueden ir en el mapa.
    fun destinosEnMapa(destinos: List<Experiencia>): List<Experiencia> =
        destinos.filter { esCoordenadaValida(it.latitud, it.longitud) }

    // Distancia en línea recta sobre la superficie de la Tierra (fórmula de Haversine). No es la
    // distancia por carretera, pero sirve para dar una idea de qué tan lejos está un destino.
    fun distanciaKm(desde: Coordenada, hasta: Coordenada): Double {
        val lat1 = Math.toRadians(desde.latitud)
        val lat2 = Math.toRadians(hasta.latitud)
        val dLat = lat2 - lat1
        val dLon = Math.toRadians(hasta.longitud - desde.longitud)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * RADIO_TIERRA_KM * asin(sqrt(a))
    }

    fun textoDistancia(km: Double): String = when {
        km < 1.0 -> "A ${(km * 1000).roundToInt()} m de ti"
        km < 10.0 -> "A ${"%.1f".format(java.util.Locale.US, km)} km de ti"
        else -> "A ${km.roundToInt()} km de ti"
    }
}
