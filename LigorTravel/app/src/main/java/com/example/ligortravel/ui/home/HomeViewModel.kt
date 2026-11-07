package com.example.ligortravel.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer

data class HomeUiState(
    val nombre: String = "",
    val ubicacion: String = "Lima, Perú",
    val destacadas: List<Experiencia> = emptyList(),
    val locales: List<Experiencia> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null,
    val buscando: Boolean = false,
    val consulta: String = "",
    val resultados: List<Experiencia> = emptyList()
)

class HomeViewModel(
    private val repository: ExperienciaRepository = ExperienciaRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Catálogo completo tal como llega de Firestore; la búsqueda filtra sobre esta lista.
    private var catalogo: List<Experiencia> = emptyList()

    init {
        cargarUsuarioActual()
        observarExperiencias()
    }

    private fun cargarUsuarioActual() {
        val usuario = SessionManager.usuarioActual ?: return
        _uiState.value = _uiState.value.copy(nombre = usuario.nombre)
    }

    private fun observarExperiencias() {
        viewModelScope.launch {
            repository.observarExperiencias().collect { resultado ->
                resultado
                    .onSuccess { lista ->
                        catalogo = lista
                        _uiState.value = _uiState.value.copy(
                            destacadas = lista.filter { it.destacada },
                            locales = lista.filterNot { it.destacada },
                            resultados = filtrar(_uiState.value.consulta),
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

    fun abrirBusqueda() {
        _uiState.value = _uiState.value.copy(buscando = true)
    }

    fun cerrarBusqueda() {
        _uiState.value = _uiState.value.copy(buscando = false, consulta = "", resultados = emptyList())
    }

    // Se llama con cada letra que escribe el usuario: filtra en memoria, sin esperar a la red.
    fun onConsultaChange(valor: String) {
        _uiState.value = _uiState.value.copy(consulta = valor, resultados = filtrar(valor))
    }

    private fun filtrar(consulta: String): List<Experiencia> {
        val buscado = normalizar(consulta)
        if (buscado.isEmpty()) return emptyList()
        return catalogo.filter { normalizar(it.titulo).contains(buscado) }
    }

    // "Cañón" -> "canon": quita tildes y pasa a minúsculas para que la búsqueda no dependa
    // de cómo escriba el usuario. NFD separa cada letra de su tilde, y \p{Mn} son esas tildes.
    private fun normalizar(texto: String): String =
        Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase()
}
