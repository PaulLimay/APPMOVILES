package com.example.ligortravel.ui.reserva

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.PaqueteRepository
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.data.ReservaRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ReservaUiState(
    // Paquete que se está reservando (se escucha en tiempo real para mostrar el precio vigente).
    val paquete: Paquete? = null,
    val cargandoPaquete: Boolean = true,
    // Error al cargar el paquete: sin paquete no se puede mostrar el formulario.
    val errorPaquete: String? = null,

    // --- Formulario ---
    val fecha: LocalDate? = null,
    val personas: Int = ReglasReserva.PERSONAS_MIN,

    // --- Envío (mismos nombres que en Login/Registro/Perfil) ---
    val cargando: Boolean = false,
    val error: String? = null,
    val reservaExitosa: Boolean = false,
    // Id del documento creado en Firestore; se muestra como código de la reserva.
    val codigoReserva: String? = null
) {
    // Propiedad calculada: no se guarda en el estado, se deriva de precio y personas cada vez que
    // se lee. Así nunca puede quedar desincronizada.
    val total: Int get() = (paquete?.precio ?: 0) * personas
}

// El id del paquete llega por el constructor: lo lee LigorNavGraph de la ruta "reserva/{paqueteId}".
class ReservaViewModel(
    private val paqueteId: String,
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val reservaRepository: ReservaRepository = ReservaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReservaUiState())
    val uiState: StateFlow<ReservaUiState> = _uiState.asStateFlow()

    init {
        observarPaquete()
    }

    private fun observarPaquete() {
        viewModelScope.launch {
            paqueteRepository.observarPaquete(paqueteId).collect { resultado ->
                resultado
                    .onSuccess { paquete ->
                        _uiState.value = _uiState.value.copy(
                            paquete = paquete,
                            cargandoPaquete = false,
                            errorPaquete = null
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            cargandoPaquete = false,
                            errorPaquete = if (e is NoSuchElementException) {
                                "Este paquete ya no está disponible"
                            } else {
                                "No se pudo cargar el paquete, verifica tu conexión"
                            }
                        )
                    }
            }
        }
    }

    fun onFechaChange(fecha: LocalDate) {
        _uiState.value = _uiState.value.copy(fecha = fecha, error = null)
    }

    // coerceIn "recorta" el valor al rango permitido: aunque llegue 0 u 11, queda en 1 o 10.
    fun onPersonasChange(personas: Int) {
        _uiState.value = _uiState.value.copy(
            personas = personas.coerceIn(ReglasReserva.PERSONAS_MIN, maximoPersonas()),
            error = null
        )
    }

    // Con pocos cupos el máximo baja; nunca menos de 1 para que el rango de coerceIn sea válido.
    private fun maximoPersonas(): Int = maxOf(
        ReglasReserva.PERSONAS_MIN,
        ReglasReserva.personasMaximas(_uiState.value.paquete?.cupos ?: ReglasReserva.PERSONAS_MAX)
    )

    fun confirmarReserva() {
        val estado = _uiState.value
        // Evita reservas duplicadas si el usuario toca el botón dos veces seguidas.
        if (estado.cargando || estado.reservaExitosa) return

        val paquete = estado.paquete
        if (paquete == null) {
            _uiState.value = estado.copy(error = "Espera a que cargue el paquete")
            return
        }
        val usuarioId = SessionManager.usuarioActual?.id
        if (usuarioId == null) {
            _uiState.value = estado.copy(error = "Tu sesión expiró, vuelve a iniciar sesión")
            return
        }
        val errorValidacion = ReglasReserva.validar(estado.fecha, estado.personas, LocalDate.now(), paquete.cupos)
        // estado.fecha == null ya lo detecta validar(); se repite aquí para que Kotlin sepa
        // que, de aquí en adelante, fecha no es null (smart cast).
        val fecha = estado.fecha
        if (errorValidacion != null || fecha == null) {
            _uiState.value = estado.copy(error = errorValidacion)
            return
        }

        _uiState.value = estado.copy(cargando = true, error = null)

        viewModelScope.launch {
            val reserva = Reserva(
                usuarioId = usuarioId,
                paqueteId = paquete.id,
                paqueteTitulo = paquete.titulo,
                fechaViaje = fecha.aTimestampUtc(),
                personas = estado.personas,
                precioUnitario = paquete.precio,
                total = paquete.precio * estado.personas,
                estado = Reserva.ESTADO_PENDIENTE_PAGO
            )
            reservaRepository.crearReserva(reserva)
                .onSuccess { id ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        reservaExitosa = true,
                        codigoReserva = id
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        error = e.message ?: "No se pudo registrar la reserva, intenta de nuevo"
                    )
                }
        }
    }
}
