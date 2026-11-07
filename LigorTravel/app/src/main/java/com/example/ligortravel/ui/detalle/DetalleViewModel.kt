package com.example.ligortravel.ui.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetalleUiState(
    val experiencia: Experiencia? = null,
    // Nombres de drawable de todas las fotos del destino: la principal primero y luego la galería.
    val fotos: List<String> = emptyList(),
    // Posición (en "fotos") de la foto que se ve en grande arriba.
    val fotoSeleccionada: Int = 0,
    val cargando: Boolean = true,
    val error: String? = null
)

// El id del destino llega por el constructor: lo lee LigorNavGraph de la ruta "detalle/{id}".
class DetalleViewModel(
    private val experienciaId: String,
    private val repository: ExperienciaRepository = ExperienciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        observarExperiencia()
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

    fun onSeleccionarFoto(indice: Int) {
        _uiState.value = _uiState.value.copy(fotoSeleccionada = indice)
    }
}
