package com.example.ligortravel.ui.admin

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AdminRepository
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.FotoRepository
import com.example.ligortravel.data.Paquete
import com.example.ligortravel.data.PaqueteRepository
import com.example.ligortravel.data.referenciaDeFoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditarPaqueteUiState(
    val formulario: FormularioPaquete = FormularioPaquete(),
    val portada: FotoFormulario? = null,
    // Destinos que el administrador puede incluir en el paquete.
    val destinosDisponibles: List<Experiencia> = emptyList(),
    val cargandoDatos: Boolean = true,
    val errorDatos: String? = null,
    val procesandoFoto: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

// paqueteId null = crear un paquete nuevo.
class EditarPaqueteViewModel(
    private val paqueteId: String?,
    private val adminRepository: AdminRepository = AdminRepository(),
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository(),
    private val paqueteRepository: PaqueteRepository = PaqueteRepository(),
    private val fotoRepository: FotoRepository = FotoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarPaqueteUiState())
    val uiState: StateFlow<EditarPaqueteUiState> = _uiState.asStateFlow()

    private var original: Paquete? = null
    private var orden = 0

    init {
        cargar()
    }

    private fun cargar() {
        viewModelScope.launch {
            val destinos = experienciaRepository.observarExperiencias().first().getOrNull()
            val paquetes = paqueteRepository.observarPaquetes().first().getOrNull()
            if (destinos == null || paquetes == null) {
                _uiState.value = _uiState.value.copy(
                    cargandoDatos = false,
                    errorDatos = "No se pudo cargar el paquete, verifica tu conexión"
                )
                return@launch
            }
            if (paqueteId == null) {
                orden = ReglasAdmin.siguienteOrden(paquetes.map { it.orden })
                _uiState.value = _uiState.value.copy(destinosDisponibles = destinos, cargandoDatos = false)
                return@launch
            }
            val paquete = paquetes.find { it.id == paqueteId }
            if (paquete == null) {
                _uiState.value = _uiState.value.copy(cargandoDatos = false, errorDatos = "Este paquete ya no existe")
                return@launch
            }
            original = paquete
            orden = paquete.orden
            _uiState.value = _uiState.value.copy(
                formulario = FormularioPaquete(
                    titulo = paquete.titulo,
                    destino = paquete.destino,
                    duracion = paquete.duracion,
                    precio = paquete.precio.toString(),
                    cupos = paquete.cupos.toString(),
                    descripcion = paquete.descripcion,
                    serviciosIncluidos = paquete.serviciosIncluidos.joinToString("\n"),
                    // Se descartan ids de destinos que ya no existen.
                    experienciaIds = paquete.experienciaIds.filter { id -> destinos.any { it.id == id } }
                ),
                portada = paquete.imagen?.let { FotoFormulario(it) },
                destinosDisponibles = destinos,
                cargandoDatos = false
            )
        }
    }

    fun onFormularioChange(formulario: FormularioPaquete) {
        _uiState.value = _uiState.value.copy(formulario = formulario, error = null)
    }

    // Marca o desmarca un destino, conservando el orden en que se eligieron.
    fun alternarDestino(destinoId: String) {
        val formulario = _uiState.value.formulario
        val ids = if (destinoId in formulario.experienciaIds) {
            formulario.experienciaIds - destinoId
        } else {
            formulario.experienciaIds + destinoId
        }
        onFormularioChange(formulario.copy(experienciaIds = ids))
    }

    fun onFotoElegida(resolver: ContentResolver, uri: Uri) {
        if (_uiState.value.procesandoFoto) return
        _uiState.value = _uiState.value.copy(procesandoFoto = true, error = null)
        viewModelScope.launch {
            val bytes = comprimirFoto(resolver, uri)
            _uiState.value = if (bytes == null) {
                _uiState.value.copy(procesandoFoto = false, error = "No se pudo usar esa foto, prueba con otra")
            } else {
                _uiState.value.copy(
                    portada = FotoFormulario(referenciaDeFoto(fotoRepository.nuevoId()), bytes),
                    procesandoFoto = false
                )
            }
        }
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.cargando || estado.procesandoFoto) return
        val errorValidacion = ReglasAdmin.validarPaquete(estado.formulario, estado.portada != null)
        val portada = estado.portada
        if (errorValidacion != null || portada == null) {
            _uiState.value = estado.copy(error = errorValidacion)
            return
        }

        val formulario = estado.formulario
        val campos = mapOf(
            "titulo" to formulario.titulo.trim(),
            "destino" to formulario.destino.trim(),
            "duracion" to formulario.duracion.trim(),
            "precio" to formulario.precio.toInt(),
            "cupos" to formulario.cupos.toInt(),
            "descripcion" to formulario.descripcion.trim(),
            "serviciosIncluidos" to ReglasAdmin.lineas(formulario.serviciosIncluidos),
            "experienciaIds" to formulario.experienciaIds,
            "imagen" to portada.referencia,
            "orden" to orden
        )
        val cambios = ReglasAdmin.cambiosDeFotos(listOfNotNull(original?.imagen), listOf(portada))

        _uiState.value = estado.copy(cargando = true, error = null)
        viewModelScope.launch {
            adminRepository.guardarPaquete(paqueteId, campos, cambios.nuevas, cambios.eliminadas)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(cargando = false, guardadoExitoso = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(cargando = false, error = e.message)
                }
        }
    }
}
