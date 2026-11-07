package com.example.ligortravel.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AuthRepository
import com.example.ligortravel.data.RegistroUsuario
import com.example.ligortravel.data.UserRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PerfilUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val editando: Boolean = false,
    val cargando: Boolean = false,
    val nombreOriginal: String = "",
    val telefonoOriginal: String = "",
    val error: String? = null,
    val guardadoExitoso: Boolean = false,
    val passwordActual: String = "",
    val passwordNuevo: String = "",
    val confirmarPassword: String = "",
    val mostrarDialogo: Boolean = false,
    val cargandoPassword: Boolean = false,
    val errorPassword: String? = null,
    val passwordActualizada: Boolean = false
)

class PerfilViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarioActual()
    }

    private fun cargarUsuarioActual() {
        val usuario = SessionManager.usuarioActual ?: return
        _uiState.value = _uiState.value.copy(
            nombre = usuario.nombre,
            email = usuario.email,
            telefono = usuario.telefono
        )
    }

    fun onNombreChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, error = null)
    }

    fun onTelefonoChange(valor: String) {
        _uiState.value = _uiState.value.copy(telefono = valor, error = null)
    }

    fun onPasswordActualChange(valor: String) {
        _uiState.value = _uiState.value.copy(passwordActual = valor, errorPassword = null)
    }
    fun onPasswordNuevoChange(valor: String) {
        _uiState.value = _uiState.value.copy(passwordNuevo = valor, errorPassword = null)
    }
    fun onConfirmarPasswordNuevoChange(valor: String) {
        _uiState.value = _uiState.value.copy(confirmarPassword = valor, errorPassword = null)
    }

    fun activarEdicion() {
        _uiState.value = _uiState.value.copy(editando = true, nombreOriginal = _uiState.value.nombre, telefonoOriginal = _uiState.value.telefono, guardadoExitoso = false)
    }
    fun cancelarEdicion() {
        _uiState.value = _uiState.value.copy(editando = false, nombre = _uiState.value.nombreOriginal, telefono = _uiState.value.telefonoOriginal)
    }
    fun abrirDialogoPassword() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogo = true,
            passwordActual = "",
            passwordNuevo = "",
            confirmarPassword = "",
            errorPassword = null,
            passwordActualizada = false
        )
    }

    fun cerrarDialogoPassword() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogo = false,
            passwordActual = "",
            passwordNuevo = "",
            confirmarPassword = "",
            errorPassword = null
        )
    }

    fun actualizarPassword() {
        val estado = _uiState.value

        if (estado.passwordActual.isBlank() || estado.passwordNuevo.isBlank() || estado.confirmarPassword.isBlank()) {
            _uiState.value = estado.copy(errorPassword = "Completa todos los campos")
            return
        }
        if (estado.passwordNuevo.length < 6) {
            _uiState.value = estado.copy(errorPassword = "La nueva contraseña debe tener al menos 6 caracteres")
            return
        }
        if (estado.passwordNuevo != estado.confirmarPassword) {
            _uiState.value = estado.copy(errorPassword = "Las contraseñas nuevas no coinciden")
            return
        }

        _uiState.value = estado.copy(cargandoPassword = true, errorPassword = null)

        viewModelScope.launch {
            authRepository.actualizarPassword(estado.passwordActual, estado.passwordNuevo)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        cargandoPassword = false,
                        mostrarDialogo = false,
                        passwordActual = "",
                        passwordNuevo = "",
                        confirmarPassword = "",
                        passwordActualizada = true
                    )
                }
                .onFailure { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargandoPassword = false,
                        errorPassword = excepcion.message ?: "No se pudo actualizar la contraseña"
                    )
                }
        }
    }

    fun guardarCambios() {
        val estado = _uiState.value
        val usuarioActual = SessionManager.usuarioActual ?: return
        if (usuarioActual.id == null) return

        if (estado.nombre.isBlank() || estado.telefono.isBlank()) {
            _uiState.value = estado.copy(error = "Completa todos los campos")
            return
        }

        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            val usuarioActualizado = RegistroUsuario(
                id = usuarioActual.id,
                nombre = estado.nombre.trim(),
                email = usuarioActual.email,
                telefono = estado.telefono.trim()
            )

            repository.guardarPerfil(usuarioActualizado)
                .onSuccess { usuario ->
                    SessionManager.iniciarSesion(usuario)
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        editando = false,
                        guardadoExitoso = true
                    )
                }
                .onFailure { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message ?: "No se pudieron guardar los cambios"
                    )
                }
        }
    }

    fun cerrarSesion() {
        authRepository.cerrarSesion()
        SessionManager.cerrarSesion()
    }
}
