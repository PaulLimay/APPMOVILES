package com.example.ligortravel.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.ChatRepository
import com.example.ligortravel.data.MAX_CARACTERES_MENSAJE
import com.example.ligortravel.data.MensajeChat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val mensajes: List<MensajeChat> = emptyList(),
    val texto: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

// El historial vive solo aquí: se conserva al abrir una ficha y volver, y se pierde al salir del
// chat, porque el ViewModel se destruye junto con esa entrada del back stack.
class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun onTextoChange(valor: String) {
        _uiState.value = _uiState.value.copy(texto = valor.take(MAX_CARACTERES_MENSAJE), error = null)
    }

    fun enviar() {
        enviarTexto(_uiState.value.texto)
    }

    fun enviarTexto(texto: String) {
        val mensaje = texto.trim()
        if (mensaje.isEmpty() || _uiState.value.cargando) return

        _uiState.value = _uiState.value.copy(
            mensajes = _uiState.value.mensajes + MensajeChat(mensaje, esDelUsuario = true),
            texto = "",
            error = null
        )
        pedirRespuesta()
    }

    // El mensaje del usuario sigue en la lista tras un error: reintentar solo repite la llamada.
    fun reintentar() {
        if (_uiState.value.cargando) return
        _uiState.value = _uiState.value.copy(error = null)
        pedirRespuesta()
    }

    private fun pedirRespuesta() {
        _uiState.value = _uiState.value.copy(cargando = true)
        viewModelScope.launch {
            repository.enviar(_uiState.value.mensajes)
                .onSuccess { respuesta ->
                    val mensaje = MensajeChat(
                        texto = respuesta.texto,
                        esDelUsuario = false,
                        recomendaciones = respuesta.recomendaciones
                    )
                    _uiState.value = _uiState.value.copy(
                        mensajes = _uiState.value.mensajes + mensaje,
                        cargando = false
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(cargando = false, error = e.message)
                }
        }
    }
}
