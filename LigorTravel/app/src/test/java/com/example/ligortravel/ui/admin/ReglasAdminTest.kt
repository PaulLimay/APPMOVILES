package com.example.ligortravel.ui.admin

import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.idDeFoto
import com.example.ligortravel.data.referenciaDeFoto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasAdminTest {

    private val destinoValido = FormularioDestino(
        titulo = "Laguna 69",
        ubicacion = "Huaraz, Áncash",
        duracion = "1 día",
        precioDesde = "120"
    )

    private val paqueteValido = FormularioPaquete(
        titulo = "Huaraz Aventura",
        destino = "Áncash",
        duracion = "3 días / 2 noches",
        precio = "199",
        cupos = "20",
        experienciaIds = listOf("1")
    )

    @Test
    fun `un destino completo es valido`() {
        assertNull(ReglasAdmin.validarDestino(destinoValido, tieneFoto = true))
    }

    @Test
    fun `un destino exige foto, nombre y precio entero`() {
        assertNotNull(ReglasAdmin.validarDestino(destinoValido, tieneFoto = false))
        assertNotNull(ReglasAdmin.validarDestino(destinoValido.copy(titulo = "ab"), tieneFoto = true))
        assertNotNull(ReglasAdmin.validarDestino(destinoValido.copy(precioDesde = "12.5"), tieneFoto = true))
        assertNotNull(ReglasAdmin.validarDestino(destinoValido.copy(precioDesde = "0"), tieneFoto = true))
    }

    @Test
    fun `un paquete puede tener cero cupos pero no negativos`() {
        assertNull(ReglasAdmin.validarPaquete(paqueteValido.copy(cupos = "0"), tieneFoto = true))
        assertNotNull(ReglasAdmin.validarPaquete(paqueteValido.copy(cupos = "-1"), tieneFoto = true))
        assertNotNull(ReglasAdmin.validarPaquete(paqueteValido.copy(cupos = ""), tieneFoto = true))
    }

    @Test
    fun `un paquete exige al menos un destino`() {
        assertNotNull(ReglasAdmin.validarPaquete(paqueteValido.copy(experienciaIds = emptyList()), tieneFoto = true))
    }

    @Test
    fun `las listas se escriben una por linea`() {
        assertEquals(listOf("Guía", "Almuerzo"), ReglasAdmin.lineas(" Guía \n\n Almuerzo\n"))
    }

    @Test
    fun `detecta los paquetes que incluyen un destino`() {
        val paquetes = listOf(
            Paquete(id = "a", experienciaIds = listOf("1", "2")),
            Paquete(id = "b", experienciaIds = listOf("3"))
        )

        assertEquals(listOf("a"), ReglasAdmin.paquetesQueIncluyen("2", paquetes).map { it.id })
        assertTrue(ReglasAdmin.paquetesQueIncluyen("9", paquetes).isEmpty())
    }

    @Test
    fun `el siguiente orden va despues del mayor`() {
        assertEquals(16, ReglasAdmin.siguienteOrden(listOf(3, 15, 7)))
        assertEquals(1, ReglasAdmin.siguienteOrden(emptyList()))
    }

    @Test
    fun `reduce la foto sin deformarla`() {
        assertEquals(1080 to 810, ReglasAdmin.dimensionesReducidas(4000, 3000))
        assertEquals(810 to 1080, ReglasAdmin.dimensionesReducidas(3000, 4000))
        assertEquals(800 to 600, ReglasAdmin.dimensionesReducidas(800, 600))
    }

    @Test
    fun `calcula que fotos subir y cuales borrar`() {
        val bytes = byteArrayOf(1, 2, 3)
        val originales = listOf("laguna_69", referenciaDeFoto("vieja"), referenciaDeFoto("sigue"))
        val actuales = listOf(
            FotoFormulario("laguna_69"),
            FotoFormulario(referenciaDeFoto("sigue")),
            FotoFormulario(referenciaDeFoto("nueva"), bytes)
        )

        val cambios = ReglasAdmin.cambiosDeFotos(originales, actuales)

        assertEquals(setOf("nueva"), cambios.nuevas.keys)
        // Los drawables locales nunca se borran: no están en la colección "fotos".
        assertEquals(listOf("vieja"), cambios.eliminadas)
    }

    @Test
    fun `distingue fotos subidas de drawables locales`() {
        assertEquals("abc", idDeFoto("foto:abc"))
        assertNull(idDeFoto("laguna_69"))
        assertNull(idDeFoto("foto:"))
        assertNull(idDeFoto(null))
    }
}
