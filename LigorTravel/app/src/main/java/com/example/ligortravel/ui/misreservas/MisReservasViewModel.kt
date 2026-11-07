package com.example.ligortravel.ui.misreservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.data.ReservaRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate

data class MisReservasUiState(
    val proximas: List<ReservaConFecha> = emptyList(),
    val realizadas: List<ReservaConFecha> = emptyList(),
    val canceladas: List<ReservaConFecha> = emptyList(),
    // Fecha usada para separar las listas; la pantalla la reutiliza para "Faltan N días".
    val hoy: LocalDate = LocalDate.now(),
    val cargando: Boolean = true,
    val error: String? = null,

    // --- Cancelación ---
    // Id de la reserva que se está cancelando (para mostrar su indicador de carga).
    val cancelandoId: String? = null,
    val errorCancelacion: String? = null
)

class MisReservasViewModel(
    private val repository: ReservaRepository = ReservaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MisReservasUiState())
    val uiState: StateFlow<MisReservasUiState> = _uiState.asStateFlow()

    init {
        observarReservas()
    }

    private fun observarReservas() {
        val usuarioId = SessionManager.usuarioActual?.id
        if (usuarioId == null) {
            _uiState.value = _uiState.value.copy(
                cargando = false,
                error = "Tu sesión expiró, vuelve a iniciar sesión"
            )
            return
        }

        viewModelScope.launch {
            repository.observarReservasDeUsuario(usuarioId).collect { resultado ->
                resultado
                    .onSuccess { reservas ->
                        // "hoy" se recalcula en cada actualización: si la pantalla queda abierta
                        // pasada la medianoche, la próxima vez que cambien los datos se reubican.
                        val hoy = LocalDate.now()
                        val separadas = separarReservas(reservas, hoy)
                        _uiState.value = _uiState.value.copy(
                            proximas = separadas.proximas,
                            realizadas = separadas.realizadas,
                            canceladas = separadas.canceladas,
                            hoy = hoy,
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = "No se pudieron cargar tus reservas, verifica tu conexión"
                        )
                    }
            }
        }
    }

    // Por qué no se puede cancelar (o null si se puede). La pantalla lo usa para decidir si
    // muestra el botón o el motivo.
    fun motivoNoCancelable(reserva: Reserva): String? =
        ReglasCancelacion.motivoNoCancelable(reserva, Instant.now())

    // No hace falta mover la reserva de pestaña a mano: el listener trae el cambio de estado.
    fun cancelar(reserva: Reserva) {
        if (_uiState.value.cancelandoId != null) return
        val motivo = motivoNoCancelable(reserva)
        if (motivo != null) {
            _uiState.value = _uiState.value.copy(errorCancelacion = motivo)
            return
        }
        _uiState.value = _uiState.value.copy(cancelandoId = reserva.id, errorCancelacion = null)
        viewModelScope.launch {
            val resultado = repository.cancelarReserva(reserva.id, Reserva.CANCELADA_POR_USUARIO, motivo = null)
            _uiState.value = _uiState.value.copy(
                cancelandoId = null,
                errorCancelacion = resultado.exceptionOrNull()?.message
            )
        }
    }

    fun limpiarErrorCancelacion() {
        _uiState.value = _uiState.value.copy(errorCancelacion = null)
    }
}
