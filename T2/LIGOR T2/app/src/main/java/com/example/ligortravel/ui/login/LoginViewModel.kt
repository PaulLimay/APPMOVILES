package com.example.ligortravel.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AuthRepository
import com.example.ligortravel.data.UserRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val loginExitoso: Boolean = false
)

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(valor: String) {
        _uiState.value = _uiState.value.copy(email = valor, error = null)
    }

    fun onPasswordChange(valor: String) {
        _uiState.value = _uiState.value.copy(password = valor, error = null)
    }

    fun iniciarSesion() {
        val estado = _uiState.value

        if (estado.email.isBlank() || estado.password.isBlank()) {
            _uiState.value = estado.copy(error = "Ingresa tu correo y contraseña")
            return
        }

        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            val uid = authRepository.iniciarSesion(estado.email.trim(), estado.password)
                .getOrElse { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message ?: "No se pudo iniciar sesión"
                    )
                    return@launch
                }

            repository.obtenerPerfil(uid)
                .onSuccess { usuario ->
                    SessionManager.iniciarSesion(usuario)
                    _uiState.value = _uiState.value.copy(cargando = false, loginExitoso = true)
                }
                .onFailure { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message ?: "No se pudo iniciar sesión"
                    )
                }
        }
    }
}
