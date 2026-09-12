package com.example.equiposfutbolcrud

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    // GET: obtener todos los equipos
    @GET("equipos")
    suspend fun obtenerEquipos(): List<Equipo>

    // POST: crear equipo
    @POST("equipos")
    suspend fun crearEquipo(
        @Body equipo: Equipo
    ): Equipo

    // PUT: editar equipo
    @PUT("equipos/{id}")
    suspend fun actualizarEquipo(
        @Path("id") id: String,
        @Body equipo: Equipo
    ): Equipo

    // DELETE: eliminar equipo
    @DELETE("equipos/{id}")
    suspend fun eliminarEquipo(
        @Path("id") id: String
    )
}