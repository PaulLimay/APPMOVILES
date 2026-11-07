package com.example.ligortravel.ui.detalle

import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.Resena
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.ui.reserva.aTimestampUtc
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class ReglasResenasTest {

    private val hoy = LocalDate.of(2026, 11, 6)
    private val paquetes = listOf(
        Paquete(id = "huaraz", experienciaIds = listOf("laguna69", "churup")),
        Paquete(id = "cusco", experienciaIds = listOf("machu"))
    )

    private fun reserva(id: String, paqueteId: String, fecha: LocalDate, estado: String = Reserva.ESTADO_PAGADA) =
        Reserva(id = id, paqueteId = paqueteId, fechaViaje = fecha.aTimestampUtc(), estado = estado)

    @Test
    fun `una resena exige estrellas y un comentario de al menos 10 caracteres`() {
        assertNull(ReglasResenas.validar(5, "Paisajes increíbles"))
        assertNotNull(ReglasResenas.validar(0, "Paisajes increíbles"))
        assertNotNull(ReglasResenas.validar(6, "Paisajes increíbles"))
        assertNotNull(ReglasResenas.validar(4, "  Bonito  "))
        assertNotNull(ReglasResenas.validar(4, "x".repeat(501)))
    }

    @Test
    fun `el promedio es suma entre total y sin resenas dice Nuevo`() {
        assertEquals(4.5, ReglasResenas.promedio(9, 2)!!, 0.0001)
        assertNull(ReglasResenas.promedio(0, 0))
        assertEquals("4.3", ReglasResenas.textoPromedio(Experiencia(sumaEstrellas = 13, totalResenas = 3)))
        assertEquals("Nuevo", ReglasResenas.textoPromedio(Experiencia()))
    }

    @Test
    fun `cuenta cuantas resenas hay de cada numero de estrellas`() {
        val resenas = listOf(Resena(estrellas = 5), Resena(estrellas = 5), Resena(estrellas = 2))

        assertEquals(mapOf(5 to 2, 4 to 0, 3 to 0, 2 to 1, 1 to 0), ReglasResenas.distribucion(resenas))
    }

    @Test
    fun `las de otros van de la mas reciente a la mas antigua y sin la propia`() {
        val resenas = listOf(
            Resena(id = "a", creadaEn = Timestamp(100, 0)),
            Resena(id = "yo", creadaEn = Timestamp(300, 0)),
            Resena(id = "b", creadaEn = Timestamp(50, 0), actualizadaEn = Timestamp(200, 0))
        )

        assertEquals(listOf("b", "a"), ReglasResenas.deOtros(resenas, "yo").map { it.id })
    }

    @Test
    fun `puede resenar quien viajo con un paquete que incluye el destino`() {
        val reservas = listOf(reserva("r1", "huaraz", hoy.minusDays(10)))

        assertEquals("r1", ReglasResenas.reservaQueHabilita("churup", reservas, paquetes, hoy)?.id)
        assertNull(ReglasResenas.reservaQueHabilita("machu", reservas, paquetes, hoy))
    }

    @Test
    fun `no habilitan las reservas futuras, de hoy ni las canceladas`() {
        val reservas = listOf(
            reserva("futura", "huaraz", hoy.plusDays(3)),
            reserva("hoy", "huaraz", hoy),
            reserva("cancelada", "huaraz", hoy.minusDays(5), Reserva.ESTADO_CANCELADA)
        )

        assertNull(ReglasResenas.reservaQueHabilita("laguna69", reservas, paquetes, hoy))
    }

    @Test
    fun `un viaje realizado aunque siguiera pendiente de pago tambien habilita`() {
        val reservas = listOf(reserva("r1", "cusco", hoy.minusDays(1), Reserva.ESTADO_PENDIENTE_PAGO))

        assertEquals("r1", ReglasResenas.reservaQueHabilita("machu", reservas, paquetes, hoy)?.id)
    }

    @Test
    fun `el nombre publico es el primer nombre y la inicial del apellido`() {
        assertEquals("Paul L.", ReglasResenas.nombrePublico("  Paul   limay Ramos "))
        assertEquals("Paul", ReglasResenas.nombrePublico("Paul"))
        assertEquals("Viajero", ReglasResenas.nombrePublico("   "))
    }
}
