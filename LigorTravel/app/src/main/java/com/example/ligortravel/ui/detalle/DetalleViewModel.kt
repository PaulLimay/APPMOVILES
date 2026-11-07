package com.example.ligortravel.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AdminRepository
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.PaqueteRepository
import com.example.ligortravel.data.Resena
import com.example.ligortravel.data.ResenaRepository
import com.example.ligortravel.data.Reserva
import com.example.ligortravel.data.ReservaRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DetalleUiState(
    val experiencia: Experiencia? = null,
    // Nombres de drawable de todas las fotos del destino: la principal primero y luego la galería.
    val fotos: List<String> = emptyList(),
    // Posición (en "fotos") de la foto que se ve en grande arriba.
    val fotoSeleccionada: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null,

    // --- Reseñas ---
    val miResena: Resena? = null,
    // Las de los demás, de la más reciente a la más antigua.
    val resenas: List<Resena> = emptyList(),
    // Cantidad de reseñas por número de estrellas (5 a 1).
    val distribucion: Map<Int, Int> = emptyMap(),
    // Reserva que permite reseñar; null si el usuario aún no viajó a este destino.
    val reservaHabilitante: Reserva? = null,
    val verificandoViaje: Boolean = true,
    val esAdmin: Boolean = false,

    // --- Formulario de reseña ---
    val formularioAbierto: Boolean = false,
    val estrellas: Int = 0,
    val comentario: String = "",
    val guardandoResena: Boolean = false,
    val errorResena: String? = null
)

// El id del destino llega por el constructor: lo lee LigorNavGraph de la ruta "detalle/{id}".
class DetalleViewModel(
    private val experienciaId: String,
    private val repository: ExperienciaRepository = ExperienciaRepository(),
    private val resenaRepository: ResenaRepository = ResenaRepository(),
    private val reservaRepository: ReservaRepository = ReservaRepository(),
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val adminRepository: AdminRepository = AdminRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    private val uid = SessionManager.usuarioActual?.id

    init {
        observarExperiencia()
        observarResenas()
        verificarViaje()
        verificarAdmin()
    }

    private fun observarExperiencia() {
        viewModelScope.launch {
            repository.observarExperiencia(experienciaId).collect { resultado ->
                resultado
                    .onSuccess { experiencia ->
                        val fotos = (listOfNotNull(experiencia.imagen) + experiencia.galeria).distinct()
                        _uiState.value = _uiState.value.copy(
                            experiencia = experiencia,
                            fotos = fotos,
                            // Si la galería cambió en Firestore y quedó más corta, no salirse de rango.
                            fotoSeleccionada = _uiState.value.fotoSeleccionada
                                .coerceAtMost((fotos.size - 1).coerceAtLeast(0)),
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = if (e is NoSuchElementException) {
                                "Este destino ya no está disponible"
                            } else {
                                "No se pudo cargar el destino, verifica tu conexión"
                            }
                        )
                    }
            }
        }
    }

    // Tiempo real: la reseña propia se separa de las demás.
    private fun observarResenas() {
        viewModelScope.launch {
            resenaRepository.observarResenas(experienciaId).collect { resultado ->
                resultado.onSuccess { todas ->
                    _uiState.value = _uiState.value.copy(
                        miResena = todas.find { it.id == uid },
                        resenas = ReglasResenas.deOtros(todas, uid),
                        distribucion = ReglasResenas.distribucion(todas)
                    )
                }
            }
        }
    }

    // ¿El usuario ya viajó a este destino? Se revisan sus reservas y los paquetes una sola vez.
    private fun verificarViaje() {
        if (uid == null) {
            _uiState.value = _uiState.value.copy(verificandoViaje = false)
            return
        }
        viewModelScope.launch {
            val reservas = reservaRepository.observarReservasDeUsuario(uid).first().getOrNull().orEmpty()
            val paquetes = paqueteRepository.observarPaquetes().first().getOrNull().orEmpty()
            _uiState.value = _uiState.value.copy(
                reservaHabilitante = ReglasResenas.reservaQueHabilita(experienciaId, reservas, paquetes, LocalDate.now()),
                verificandoViaje = false
            )
        }
    }

    private fun verificarAdmin() {
        if (uid == null) return
        viewModelScope.launch {
            adminRepository.esAdmin(uid).onSuccess { esAdmin ->
                _uiState.value = _uiState.value.copy(esAdmin = esAdmin)
            }
        }
    }

    fun onSeleccionarFoto(indice: Int) {
        _uiState.value = _uiState.value.copy(fotoSeleccionada = indice)
    }

    // Al editar, el formulario arranca con lo que ya había escrito.
    fun abrirFormulario() {
        val mia = _uiState.value.miResena
        _uiState.value = _uiState.value.copy(
            formularioAbierto = true,
            estrellas = mia?.estrellas ?: 0,
            comentario = mia?.comentario.orEmpty(),
            errorResena = null
        )
    }

    fun cerrarFormulario() {
        _uiState.value = _uiState.value.copy(formularioAbierto = false, errorResena = null)
    }

    fun onEstrellasChange(estrellas: Int) {
        _uiState.value = _uiState.value.copy(estrellas = estrellas, errorResena = null)
    }

    fun onComentarioChange(comentario: String) {
        _uiState.value = _uiState.value.copy(
            comentario = comentario.take(ReglasResenas.COMENTARIO_MAX),
            errorResena = null
        )
    }

    fun guardarResena() {
        val estado = _uiState.value
        if (estado.guardandoResena) return
        val usuario = SessionManager.usuarioActual
        val reserva = estado.reservaHabilitante
        if (usuario?.id == null || reserva == null) {
            _uiState.value = estado.copy(errorResena = "Podrás reseñar este destino después de visitarlo")
            return
        }
        val errorValidacion = ReglasResenas.validar(estado.estrellas, estado.comentario)
        if (errorValidacion != null) {
            _uiState.value = estado.copy(errorResena = errorValidacion)
            return
        }

        _uiState.value = estado.copy(guardandoResena = true, errorResena = null)
        viewModelScope.launch {
            resenaRepository.guardarResena(
                destinoId = experienciaId,
                usuarioId = usuario.id,
                nombre = ReglasResenas.nombrePublico(usuario.nombre),
                estrellas = estado.estrellas,
                comentario = estado.comentario.trim(),
                reservaId = reserva.id
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(guardandoResena = false, formularioAbierto = false)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(guardandoResena = false, errorResena = e.message)
                }
        }
    }

    // El autor borra la suya; el administrador puede borrar cualquiera (moderación).
    fun eliminarResena(resena: Resena) {
        if (_uiState.value.guardandoResena) return
        if (resena.id != uid && !_uiState.value.esAdmin) return
        _uiState.value = _uiState.value.copy(guardandoResena = true, errorResena = null)
        viewModelScope.launch {
            val resultado = resenaRepository.eliminarResena(experienciaId, resena.id)
            _uiState.value = _uiState.value.copy(
                guardandoResena = false,
                errorResena = resultado.exceptionOrNull()?.message
            )
        }
    }
}
