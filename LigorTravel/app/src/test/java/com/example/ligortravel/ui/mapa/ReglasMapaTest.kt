package com.example.ligortravel.ui.mapa

import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.ui.admin.ReglasAdmin
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasMapaTest {

    private val lima = Coordenada(-12.0464, -77.0428)
    private val cusco = Coordenada(-13.5319, -71.9675)

    @Test
    fun `la distancia de Lima a Cusco es de unos 570 km en linea recta`() {
        val km = ReglasMapa.distanciaKm(lima, cusco)
        assertTrue("fue $km", km in 560.0..590.0)
    }

    @Test
    fun `la distancia a uno mismo es cero y no depende del sentido`() {
        assertEquals(0.0, ReglasMapa.distanciaKm(lima, lima), 0.0001)
        assertEquals(ReglasMapa.distanciaKm(lima, cusco), ReglasMapa.distanciaKm(cusco, lima), 0.0001)
    }

    @Test
    fun `el texto de distancia usa metros, un decimal o kilometros enteros`() {
        assertEquals("A 850 m de ti", ReglasMapa.textoDistancia(0.85))
        assertEquals("A 3.4 km de ti", ReglasMapa.textoDistancia(3.42))
        assertEquals("A 571 km de ti", ReglasMapa.textoDistancia(571.3))
    }

    @Test
    fun `solo van al mapa los destinos con coordenadas validas`() {
        val destinos = listOf(
            Experiencia(id = "ok", latitud = -9.0114, longitud = -77.6153),
            Experiencia(id = "sinDatos"),
            Experiencia(id = "soloLatitud", latitud = -9.0),
            Experiencia(id = "cero", latitud = 0.0, longitud = 0.0),
            Experiencia(id = "fueraDeRango", latitud = -95.0, longitud = 10.0)
        )

        assertEquals(listOf("ok"), ReglasMapa.destinosEnMapa(destinos).map { it.id })
    }

    @Test
    fun `en el formulario las coordenadas son opcionales pero van juntas`() {
        assertNull(ReglasAdmin.validarCoordenadas("", ""))
        assertNull(ReglasAdmin.validarCoordenadas("-9,0114", "-77.6153"))
        assertNotNull(ReglasAdmin.validarCoordenadas("-9.0114", ""))
        assertNotNull(ReglasAdmin.validarCoordenadas("norte", "-77"))
        assertNotNull(ReglasAdmin.validarCoordenadas("-200", "-77"))
    }

    @Test
    fun `el punto cero no se acepta como coordenada`() {
        assertFalse(ReglasMapa.esCoordenadaValida(0.0, 0.0))
        assertTrue(ReglasMapa.esCoordenadaValida(0.0, -77.0))
    }
}
