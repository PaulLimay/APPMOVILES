package com.example.ligortravel.ui.paquetes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.PaqueteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class DetallePaqueteUiState(
    // El paquete con sus experiencias ya resueltas y el ahorro calculado (igual que en la lista).
    val resumen: PaqueteResumen? = null,
    val cargando: Boolean = true,
    val error: String? = null
)

// El id del paquete llega por el constructor: lo lee LigorNavGraph de la ruta
// "detalle_paquete/{paqueteId}".
class DetallePaqueteViewModel(
    private val paqueteId: String,
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetallePaqueteUiState())
    val uiState: StateFlow<DetallePaqueteUiState> = _uiState.asStateFlow()

    init {
        observarPaquete()
    }

    private fun observarPaquete() {
        viewModelScope.launch {
            // Mismo cruce que PaquetesViewModel, pero escuchando un solo paquete: si cambia el
            // paquete o cualquiera de sus experiencias en Firestore, la pantalla se actualiza.
            combine(
                paqueteRepository.observarPaquete(paqueteId),
                experienciaRepository.observarExperiencias()
            ) { resultadoPaquete, resultadoExperiencias ->
                resultadoPaquete.mapCatching { paquete ->
                    val experienciasPorId = resultadoExperiencias.getOrThrow().associateBy { it.id }
                    armarResumen(paquete, experienciasPorId)
                }
            }.collect { resultado ->
                resultado
                    .onSuccess { resumen ->
                        _uiState.value = _uiState.value.copy(
                            resumen = resumen,
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = if (e is NoSuchElementException) {
                                "Este paquete ya no está disponible"
                            } else {
                                "No se pudo cargar el paquete, verifica tu conexión"
                            }
                        )
                    }
            }
        }
    }
}
