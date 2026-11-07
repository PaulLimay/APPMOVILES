package com.example.ligortravel.ui.paquetes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.PaqueteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

// Lo que la pantalla necesita de cada paquete: el paquete en sí, más sus experiencias ya
// "resueltas" (los ids convertidos en objetos Experiencia con título, duración y precio).
data class PaqueteResumen(
    val paquete: Paquete,
    val experiencias: List<Experiencia>,
    // Lo que costarían las experiencias compradas por separado.
    val precioPorSeparado: Int
) {
    val ahorro: Int get() = precioPorSeparado - paquete.precio
}

data class PaquetesUiState(
    val paquetes: List<PaqueteResumen> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

class PaquetesViewModel(
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaquetesUiState())
    val uiState: StateFlow<PaquetesUiState> = _uiState.asStateFlow()

    init {
        observarPaquetes()
    }

    private var trabajoCarga: Job? = null

    private fun observarPaquetes() {
        /*viewModelScope.launch {
            // combine escucha los dos Flows a la vez: cada vez que CUALQUIERA de los dos emite
            // (cambió un paquete o cambió una experiencia en Firestore), vuelve a ejecutar este
            // bloque con el último valor de cada uno. Así los ids de cada paquete se cruzan
            // siempre con la versión más reciente de las experiencias.
            combine(
                paqueteRepository.observarPaquetes(),
                experienciaRepository.observarExperiencias()
            ) { resultadoPaquetes, resultadoExperiencias ->
                // mapCatching: si alguno de los dos falló, getOrThrow lanza el error y el
                // resultado final queda como failure (lo maneja onFailure de abajo).
                resultadoPaquetes.mapCatching { paquetes ->
                    val experienciasPorId = resultadoExperiencias.getOrThrow().associateBy { it.id }
                    paquetes.map { armarResumen(it, experienciasPorId) }
                }
            }.collect { resultado ->
                resultado
                    .onSuccess { resumenes ->
                        _uiState.value = _uiState.value.copy(
                            paquetes = resumenes,
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = "No se pudieron cargar los paquetes, verifica tu conexión"
                        )
                    }
            }
        }*/
        trabajoCarga?.cancel()
        trabajoCarga = viewModelScope.launch {
            combine(
                paqueteRepository.observarPaquetes(),
                experienciaRepository.observarExperiencias()
            ) { resultadoPaquetes, resultadoExperiencias ->
                // mapCatching: si alguno de los dos falló, getOrThrow lanza el error y el
                // resultado final queda como failure (lo maneja onFailure de abajo).
                resultadoPaquetes.mapCatching { paquetes ->
                    val experienciasPorId = resultadoExperiencias.getOrThrow().associateBy { it.id }
                    paquetes.map { armarResumen(it, experienciasPorId) }
                }
            }.collect { resultado ->
                resultado
                    .onSuccess { resumenes ->
                        _uiState.value = _uiState.value.copy(
                            paquetes = resumenes,
                            cargando = false,
                            error = null
                        )
                    }
                    .onFailure {
                        _uiState.value = _uiState.value.copy(
                            cargando = false,
                            error = "No se pudieron cargar los paquetes, verifica tu conexión"
                        )
                    }
            }
        }
    }
    fun reintentar() {
        _uiState.value = _uiState.value.copy(cargando = true, error = null)
        observarPaquetes()
    }

}

// Fuera de la clase (y sin private) para que DetallePaqueteViewModel arme el resumen de la misma
// forma, sin duplicar la lógica del ahorro.
// mapNotNull descarta los ids que no existen en "experiencias" (por ejemplo, si se borró una
// experiencia desde la consola): el paquete se muestra igual, sin esa experiencia.
fun armarResumen(paquete: Paquete, experienciasPorId: Map<String, Experiencia>): PaqueteResumen {
    val experiencias = paquete.experienciaIds.mapNotNull { experienciasPorId[it] }
    return PaqueteResumen(
        paquete = paquete,
        experiencias = experiencias,
        precioPorSeparado = experiencias.sumOf { it.precioDesde }
    )
}
