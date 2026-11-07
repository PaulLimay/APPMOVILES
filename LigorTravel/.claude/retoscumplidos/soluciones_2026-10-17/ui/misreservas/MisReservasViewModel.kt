package com.example.ligortravel.ui.misreservas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.ReservaRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MisReservasUiState(
    val proximas: List<ReservaConFecha> = emptyList(),
    val realizadas: List<ReservaConFecha> = emptyList(),
    // Fecha usada para separar las listas; la pantalla la reutiliza para "Faltan N días".
    val hoy: LocalDate = LocalDate.now(),
    val cargando: Boolean = true,
    val error: String? = null
)

class MisReservasViewModel(
    private val repository: ReservaRepository = ReservaRepository()
) : ViewModel() {


    private val _uiState = MutableStateFlow(MisReservasUiState())
    val uiState: StateFlow<MisReservasUiState> = _uiState.asStateFlow()

    private var trabajoCarga: Job? = null

    init {
        observarReservas()
    }

    private fun observarReservas() {
        trabajoCarga?.cancel()
        val usuarioId = SessionManager.usuarioActual?.id
        if (usuarioId == null) {
            _uiState.value = _uiState.value.copy(
                cargando = false,
                error = "Tu sesión expiró, vuelve a iniciar sesión"
            )
            return
        }

        trabajoCarga = viewModelScope.launch {

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
    fun reintentarReservas(){
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        observarReservas()
    }
}
