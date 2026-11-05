package com.example.ligortravel.ui.registro

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

data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmarPassword: String = "",
    val cargando: Boolean = false,
    val error: String? = null,
    val registroExitoso: Boolean = false
)

class RegistroViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val repository: UserRepository = UserRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun onNombreChange(valor: String) {
        _uiState.value = _uiState.value.copy(nombre = valor, error = null)
    }

    fun onEmailChange(valor: String) {
        _uiState.value = _uiState.value.copy(email = valor, error = null)
    }

    fun onTelefonoChange(valor: String) {
        _uiState.value = _uiState.value.copy(telefono = valor, error = null)
    }

    fun onPasswordChange(valor: String) {
        _uiState.value = _uiState.value.copy(password = valor, error = null)
    }

    fun onConfirmarPasswordChange(valor: String) {
        _uiState.value = _uiState.value.copy(confirmarPassword = valor, error = null)
    }

    fun registrar() {
        val estado = _uiState.value

        val errorValidacion = validar(estado)
        if (errorValidacion != null) {
            _uiState.value = estado.copy(error = errorValidacion)
            return
        }

        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            // Si guardarPerfil() falla después de esto, la cuenta de Auth queda sin perfil en Firestore.
            val uid = authRepository.crearCuenta(estado.email.trim(), estado.password)
                .getOrElse { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message ?: "No se pudo completar el registro"
                    )
                    return@launch
                }

            val nuevoUsuario = RegistroUsuario(
                id = uid,
                nombre = estado.nombre.trim(),
                email = estado.email.trim(),
                telefono = estado.telefono.trim()
            )

            repository.guardarPerfil(nuevoUsuario)
                .onSuccess { usuario ->
                    SessionManager.iniciarSesion(usuario)
                    _uiState.value = _uiState.value.copy(cargando = false, registroExitoso = true)
                }
                .onFailure { excepcion ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = excepcion.message ?: "No se pudo completar el registro"
                    )
                }
        }
    }

    private fun validar(estado: RegistroUiState): String? = when {
        estado.nombre.isBlank() -> "El nombre es obligatorio"
        !emailRegex.matches(estado.email.trim()) -> "Ingresa un correo válido"
        estado.telefono.trim().let { tel ->
            tel.length !=9 || !tel.startsWith("9") || !tel.all{
                it.isDigit()} } -> "El teléfono debe empezar con 9"
        estado.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
        estado.password != estado.confirmarPassword -> "Las contraseñas no coinciden"
        else -> null
    }
}
