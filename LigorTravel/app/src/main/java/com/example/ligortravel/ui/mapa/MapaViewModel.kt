package com.example.ligortravel.ui.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MapaUiState(
    // Destinos con coordenadas: los que se dibujan como marcadores.
    val destinos: List<Experiencia> = emptyList(),
    // Destinos que el administrador aún no ubicó: no aparecen en el mapa.
    val sinUbicacion: Int = 0,
    val seleccionado: Experiencia? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

class MapaViewModel(
    private val repository: ExperienciaRepository = ExperienciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapaUiState())
    val uiState: StateFlow<MapaUiState> = _uiState.asStateFlow()

    init {
        observarDestinos()
    }

    // Tiempo real: si el administrador agrega o mueve un destino, el marcador cambia solo.
    private fun observarDestinos() {
        viewModelScope.launch {
            repository.observarExperiencias().collect { resultado ->
                resultado
                    .onSuccess { todos ->
                        val enMapa = ReglasMapa.destinosEnMapa(todos)
                        val seleccionadoId = _uiState.value.seleccionado?.id
                        _uiState.value = _uiState.value.copy(
                            destinos = enMapa,
                            sinUbicacion = todos.size - enMapa.size,
                            // Si el destino elegido cambió o se eliminó, se actualiza la tarjeta.
                            seleccionado = enMapa.find { it.id == seleccionadoId },
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = "No se pudieron cargar los destinos, verifica tu conexión"
                        )
                    }
            }
        }
    }

    fun seleccionar(destinoId: String?) {
        _uiState.value = _uiState.value.copy(
            seleccionado = _uiState.value.destinos.find { it.id == destinoId }
        )
    }
}
