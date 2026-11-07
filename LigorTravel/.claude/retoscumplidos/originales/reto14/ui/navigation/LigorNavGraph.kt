package com.example.ligortravel.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ligortravel.ui.admin.AdminScreen
import com.example.ligortravel.ui.admin.EditarDestinoScreen
import com.example.ligortravel.ui.admin.EditarPaqueteScreen
import com.example.ligortravel.ui.auth.AuthScreen
import com.example.ligortravel.ui.chat.ChatScreen
import com.example.ligortravel.ui.detalle.DetalleScreen
import com.example.ligortravel.ui.favoritos.FavoritosScreen
import com.example.ligortravel.ui.home.HomeScreen
import com.example.ligortravel.ui.mapa.MapaScreen
import com.example.ligortravel.ui.misreservas.MisReservasScreen
import com.example.ligortravel.ui.paquetes.DetallePaqueteScreen
import com.example.ligortravel.ui.paquetes.PaquetesScreen
import com.example.ligortravel.ui.perfil.PerfilScreen
import com.example.ligortravel.ui.reserva.ReservaScreen
import com.example.ligortravel.ui.splash.SplashScreen

object Rutas {
    const val SPLASH = "splash"
    const val AUTH = "auth"
    const val HOME = "home"
    const val PERFIL = "perfil"
    const val PAQUETES = "paquetes"
    const val MIS_RESERVAS = "mis_reservas"
    const val CHAT = "chat"
    const val ADMIN = "admin"
    const val MAPA = "mapa"
    const val FAVORITOS = "favoritos"

    // Ruta con argumento: {experienciaId} es un "hueco" que se rellena al navegar.
    const val ARG_EXPERIENCIA_ID = "experienciaId"
    const val DETALLE = "detalle/{$ARG_EXPERIENCIA_ID}"

    // Arma la ruta concreta, ej. detalle("abc123") -> "detalle/abc123".
    fun detalle(experienciaId: String) = "detalle/${Uri.encode(experienciaId)}"

    // Mismo mecanismo para el detalle de un paquete y su reserva.
    const val ARG_PAQUETE_ID = "paqueteId"
    const val DETALLE_PAQUETE = "detalle_paquete/{$ARG_PAQUETE_ID}"
    const val RESERVA = "reserva/{$ARG_PAQUETE_ID}"

    fun detallePaquete(paqueteId: String) = "detalle_paquete/${Uri.encode(paqueteId)}"
    fun reserva(paqueteId: String) = "reserva/${Uri.encode(paqueteId)}"

    // Formularios del administrador. "?id=..." es un argumento OPCIONAL: sin él, el formulario
    // crea un elemento nuevo; con él, edita el existente.
    const val ARG_ID = "id"
    const val EDITAR_DESTINO = "admin/destino?$ARG_ID={$ARG_ID}"
    const val EDITAR_PAQUETE = "admin/paquete?$ARG_ID={$ARG_ID}"

    fun editarDestino(id: String?) = if (id == null) "admin/destino" else "admin/destino?$ARG_ID=${Uri.encode(id)}"
    fun editarPaquete(id: String?) = if (id == null) "admin/paquete" else "admin/paquete?$ARG_ID=${Uri.encode(id)}"
}

@Composable
fun LigorNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Rutas.SPLASH) {

        composable(Rutas.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(Rutas.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.AUTH) {
            AuthScreen(
                onAutenticado = {
                    navController.navigate(Rutas.HOME) {
                        popUpTo(Rutas.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.HOME) {
            HomeScreen(
                onIrAPerfil = {
                    navController.navigate(Rutas.PERFIL)
                },
                onIrAPaquetes = {
                    navController.navigate(Rutas.PAQUETES)
                },
                // Sin popUpTo: el Home queda debajo en el back stack y "atrás" vuelve a él.
                onAbrirDetalle = { experienciaId ->
                    navController.navigate(Rutas.detalle(experienciaId))
                },
                onIrAlChat = {
                    navController.navigate(Rutas.CHAT)
                },
                onIrAlMapa = {
                    navController.navigate(Rutas.MAPA)
                },
                onIrAFavoritos = {
                    navController.navigate(Rutas.FAVORITOS)
                }
            )
        }

        composable(Rutas.MAPA) {
            MapaScreen(
                onVolver = { navController.popBackStack() },
                // La ficha se apila encima: "atrás" vuelve al mapa.
                onAbrirDestino = { experienciaId ->
                    navController.navigate(Rutas.detalle(experienciaId))
                }
            )
        }

        composable(Rutas.CHAT) {
            ChatScreen(
                onVolver = { navController.popBackStack() },
                // Las fichas se apilan encima: "atrás" vuelve al chat con la conversación intacta.
                onAbrirDestino = { experienciaId ->
                    navController.navigate(Rutas.detalle(experienciaId))
                },
                onAbrirPaquete = { paqueteId ->
                    navController.navigate(Rutas.detallePaquete(paqueteId))
                }
            )
        }

        composable(Rutas.PAQUETES) {
            PaquetesScreen(
                onVolver = { navController.popBackStack() },
                // Paquetes -> Detalle del paquete: se apila encima, y "atrás" vuelve a Paquetes.
                onAbrirPaquete = { paqueteId ->
                    navController.navigate(Rutas.detallePaquete(paqueteId))
                }
            )
        }

        composable(
            route = Rutas.DETALLE_PAQUETE,
            arguments = listOf(navArgument(Rutas.ARG_PAQUETE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val paqueteId = backStackEntry.arguments?.getString(Rutas.ARG_PAQUETE_ID).orEmpty()
            DetallePaqueteScreen(
                paqueteId = paqueteId,
                onVolver = { navController.popBackStack() },
                // Detalle del paquete -> Detalle de una experiencia o -> Reserva, ambos apilados.
                onAbrirExperiencia = { experienciaId ->
                    navController.navigate(Rutas.detalle(experienciaId))
                },
                onReservar = { id ->
                    navController.navigate(Rutas.reserva(id))
                }
            )
        }
        composable (
            route = Rutas.FAVORITOS
        ){
            FavoritosScreen(onVolver = {navController.popBackStack()})
        }

        composable(
            route = Rutas.RESERVA,
            arguments = listOf(navArgument(Rutas.ARG_PAQUETE_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val paqueteId = backStackEntry.arguments?.getString(Rutas.ARG_PAQUETE_ID).orEmpty()
            ReservaScreen(
                paqueteId = paqueteId,
                onVolver = { navController.popBackStack() },
                // Tras reservar: saca del back stack todo lo que hay encima de Home (Paquetes,
                // detalle del paquete y Reserva) y vuelve a él. Así "atrás" no regresa al
                // formulario de una reserva ya confirmada.
                onIrAlInicio = { navController.popBackStack(Rutas.HOME, inclusive = false) },
                // Igual, pero después abre el historial encima de Home: "atrás" desde Mis
                // reservas vuelve al Home y no al formulario.
                onVerMisReservas = {
                    navController.popBackStack(Rutas.HOME, inclusive = false)
                    navController.navigate(Rutas.MIS_RESERVAS)
                }
            )
        }

        composable(Rutas.MIS_RESERVAS) {
            MisReservasScreen(
                onVolver = { navController.popBackStack() },
                onVerPaquetes = { navController.navigate(Rutas.PAQUETES) }
            )
        }

        composable(
            route = Rutas.DETALLE,
            arguments = listOf(navArgument(Rutas.ARG_EXPERIENCIA_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val experienciaId = backStackEntry.arguments?.getString(Rutas.ARG_EXPERIENCIA_ID).orEmpty()
            DetalleScreen(
                experienciaId = experienciaId,
                onVolver = { navController.popBackStack() }
            )
        }

        composable(Rutas.PERFIL) {
            PerfilScreen(
                onCerrarSesion = {
                    navController.navigate(Rutas.AUTH) {
                        popUpTo(Rutas.HOME) { inclusive = true }
                    }
                },
                // Perfil -> Mis reservas: se apila, y "atrás" vuelve a Perfil.
                onIrAMisReservas = { navController.navigate(Rutas.MIS_RESERVAS) },
                onIrAAdmin = { navController.navigate(Rutas.ADMIN) }
            )
        }

        composable(Rutas.ADMIN) {
            AdminScreen(
                onVolver = { navController.popBackStack() },
                onEditarDestino = { id -> navController.navigate(Rutas.editarDestino(id)) },
                onEditarPaquete = { id -> navController.navigate(Rutas.editarPaquete(id)) }
            )
        }

        composable(
            route = Rutas.EDITAR_DESTINO,
            arguments = listOf(navArgument(Rutas.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            EditarDestinoScreen(
                destinoId = backStackEntry.arguments?.getString(Rutas.ARG_ID),
                onVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = Rutas.EDITAR_PAQUETE,
            arguments = listOf(navArgument(Rutas.ARG_ID) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            EditarPaqueteScreen(
                paqueteId = backStackEntry.arguments?.getString(Rutas.ARG_ID),
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
