package com.example.ligortravel.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AdminRepository
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.PaqueteRepository
import com.example.ligortravel.data.RegistroUsuario
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.data.ReservaRepository
import com.example.ligortravel.session.SessionManager
import com.example.ligortravel.ui.misreservas.ReglasCancelacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AdminUiState(
    // null mientras se verifica el rol.
    val esAdmin: Boolean? = null,
    val destinos: List<Experiencia> = emptyList(),
    val paquetes: List<Paquete> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val eliminando: Boolean = false,

    // --- Reservas ---
    val reservas: List<Reserva> = emptyList(),
    // uid -> perfil, para mostrar quién hizo cada reserva.
    val usuarios: Map<String, RegistroUsuario> = emptyMap(),
    // Id de la reserva en la que se está registrando un pago o una cancelación.
    val procesandoReservaId: String? = null
)

class AdminViewModel(
    private val adminRepository: AdminRepository = AdminRepository(),
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository(),
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val reservaRepository: ReservaRepository = ReservaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        verificarRol()
    }

    // Comprobar el rol aquí solo decide qué mostrar. Quien de verdad bloquea las escrituras de
    // un usuario que no es administrador son las reglas de Firestore.
    private fun verificarRol() {
        val uid = SessionManager.usuarioActual?.id
        if (uid == null) {
            _uiState.value = _uiState.value.copy(esAdmin = false, cargando = false)
            return
        }
        viewModelScope.launch {
            adminRepository.esAdmin(uid)
                .onSuccess { esAdmin ->
                    _uiState.value = _uiState.value.copy(esAdmin = esAdmin, cargando = esAdmin)
                    if (esAdmin) {
                        observarCatalogo()
                        observarReservas()
                        cargarUsuarios()
                    }
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(esAdmin = false, cargando = false, error = e.message)
                }
        }
    }

    private fun observarCatalogo() {
        viewModelScope.launch {
            combine(
                experienciaRepository.observarExperiencias(),
                paqueteRepository.observarPaquetes()
            ) { destinos, paquetes -> destinos to paquetes }
                .collect { (destinos, paquetes) ->
                    val listaDestinos = destinos.getOrNull()
                    val listaPaquetes = paquetes.getOrNull()
                    _uiState.value = if (listaDestinos != null && listaPaquetes != null) {
                        _uiState.value.copy(destinos = listaDestinos, paquetes = listaPaquetes, cargando = false)
                    } else {
                        _uiState.value.copy(
                            cargando = false,
                            error = "No se pudo cargar el catálogo, verifica tu conexión"
                        )
                    }
                }
        }
    }

    private fun observarReservas() {
        viewModelScope.launch {
            reservaRepository.observarTodasLasReservas().collect { resultado ->
                resultado
                    .onSuccess { reservas ->
                        _uiState.value = _uiState.value.copy(reservas = reservas)
                        // Si aparece una reserva de un usuario que no se conocía, se recargan.
                        if (reservas.any { it.usuarioId !in _uiState.value.usuarios }) cargarUsuarios()
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(error = "No se pudieron cargar las reservas")
                    }
            }
        }
    }

    private var cargandoUsuarios = false

    private fun cargarUsuarios() {
        if (cargandoUsuarios) return
        cargandoUsuarios = true
        viewModelScope.launch {
            adminRepository.obtenerUsuarios().onSuccess { usuarios ->
                _uiState.value = _uiState.value.copy(usuarios = usuarios)
            }
            cargandoUsuarios = false
        }
    }

    // Cobro simulado: solo registra el método; no hay dinero real de por medio.
    fun registrarPago(reserva: Reserva, metodoPago: String) {
        if (!ReglasCancelacion.puedeRegistrarPago(reserva, LocalDate.now())) return
        procesarReserva(reserva) { reservaRepository.registrarPago(reserva.id, metodoPago) }
    }

    fun cancelarReserva(reserva: Reserva, motivo: String) {
        if (!ReglasCancelacion.puedeCancelarAdmin(reserva, LocalDate.now())) return
        procesarReserva(reserva) {
            reservaRepository.cancelarReserva(reserva.id, Reserva.CANCELADA_POR_ADMIN, motivo)
        }
    }

    private fun procesarReserva(reserva: Reserva, accion: suspend () -> Result<Unit>) {
        if (_uiState.value.procesandoReservaId != null) return
        _uiState.value = _uiState.value.copy(procesandoReservaId = reserva.id, error = null)
        viewModelScope.launch {
            val resultado = accion()
            _uiState.value = _uiState.value.copy(
                procesandoReservaId = null,
                error = resultado.exceptionOrNull()?.message
            )
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun eliminarDestino(destino: Experiencia) {
        val enUso = ReglasAdmin.paquetesQueIncluyen(destino.id, _uiState.value.paquetes)
        if (enUso.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "No se puede eliminar \"${destino.titulo}\": lo incluyen " +
                    enUso.joinToString { it.titulo } + ". Quítalo primero de esos paquetes."
            )
            return
        }
        ejecutarEliminacion { adminRepository.eliminarDestino(destino) }
    }

    fun eliminarPaquete(paquete: Paquete) {
        ejecutarEliminacion { adminRepository.eliminarPaquete(paquete) }
    }

    // No hace falta actualizar las listas a mano: el listener de Firestore avisa del cambio.
    private fun ejecutarEliminacion(accion: suspend () -> Result<Unit>) {
        if (_uiState.value.eliminando) return
        _uiState.value = _uiState.value.copy(eliminando = true, error = null)
        viewModelScope.launch {
            val resultado = accion()
            _uiState.value = _uiState.value.copy(
                eliminando = false,
                error = resultado.exceptionOrNull()?.message
            )
        }
    }
}
