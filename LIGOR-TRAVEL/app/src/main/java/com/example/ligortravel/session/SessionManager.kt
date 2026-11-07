package com.example.ligortravel.session

import com.example.ligortravel.data.RegistroUsuario

object SessionManager {

    var usuarioActual: RegistroUsuario? = null
        private set

    val estaAutenticado: Boolean
        get() = usuarioActual != null

    fun iniciarSesion(usuario: RegistroUsuario) {
        usuarioActual = usuario
    }

    fun cerrarSesion() {
        usuarioActual = null
    }
}
