package com.example.ligortravel.ui.admin

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ligortravel.data.AdminRepository
import com.example.ligortravel.data.Experiencia
import com.example.ligortravel.data.ExperienciaRepository
import com.example.ligortravel.data.FotoRepository
import com.example.ligortravel.data.referenciaDeFoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class EditarDestinoUiState(
    val formulario: FormularioDestino = FormularioDestino(),
    val fotoPrincipal: FotoFormulario? = null,
    val galeria: List<FotoFormulario> = emptyList(),
    val cargandoDatos: Boolean = true,
    val errorDatos: String? = null,
    val procesandoFoto: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val guardadoExitoso: Boolean = false
)

// destinoId null = crear un destino nuevo.
class EditarDestinoViewModel(
    private val destinoId: String?,
    private val adminRepository: AdminRepository = AdminRepository(),
    private val experienciaRepository: ExperienciaRepository = ExperienciaRepository(),
    private val fotoRepository: FotoRepository = FotoRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarDestinoUiState())
    val uiState: StateFlow<EditarDestinoUiState> = _uiState.asStateFlow()

    // Destino tal como estaba guardado: sirve para saber qué fotos quitó el administrador.
    private var original: Experiencia? = null
    private var orden = 0

    init {
        cargar()
    }

    // first() toma la lista una sola vez: el formulario no debe cambiar mientras se edita.
    private fun cargar() {
        viewModelScope.launch {
            experienciaRepository.observarExperiencias().first()
                .onSuccess { destinos ->
                    if (destinoId == null) {
                        orden = ReglasAdmin.siguienteOrden(destinos.map { it.orden })
                        _uiState.value = _uiState.value.copy(cargandoDatos = false)
                        return@onSuccess
                    }
                    val destino = destinos.find { it.id == destinoId }
                    if (destino == null) {
                        _uiState.value = _uiState.value.copy(
                            cargandoDatos = false,
                            errorDatos = "Este destino ya no existe"
                        )
                        return@onSuccess
                    }
                    original = destino
                    orden = destino.orden
                    _uiState.value = _uiState.value.copy(
                        formulario = FormularioDestino(
                            titulo = destino.titulo,
                            ubicacion = destino.ubicacion,
                            categoria = destino.categoria.orEmpty(),
                            duracion = destino.duracion,
                            precioDesde = destino.precioDesde.toString(),
                            descripcion = destino.descripcion,
                            incluye = destino.incluye.joinToString("\n"),
                            mejorEpoca = destino.mejorEpoca.orEmpty(),
                            destacada = destino.destacada,
                            latitud = destino.latitud?.toString().orEmpty(),
                            longitud = destino.longitud?.toString().orEmpty()
                        ),
                        fotoPrincipal = destino.imagen?.let { FotoFormulario(it) },
                        galeria = destino.galeria.map { FotoFormulario(it) },
                        cargandoDatos = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        cargandoDatos = false,
                        errorDatos = "No se pudo cargar el destino, verifica tu conexión"
                    )
                }
        }
    }

    // Recibe el formulario completo ya modificado (ej. formulario.copy(titulo = valor)): así no
    // hace falta un método onXChange por cada uno de los diez campos.
    fun onFormularioChange(formulario: FormularioDestino) {
        _uiState.value = _uiState.value.copy(formulario = formulario, error = null)
    }

    fun onFotoElegida(resolver: ContentResolver, uri: Uri, esPrincipal: Boolean) {
        if (_uiState.value.procesandoFoto) return
        _uiState.value = _uiState.value.copy(procesandoFoto = true, error = null)
        viewModelScope.launch {
            val bytes = comprimirFoto(resolver, uri)
            _uiState.value = if (bytes == null) {
                _uiState.value.copy(procesandoFoto = false, error = "No se pudo usar esa foto, prueba con otra")
            } else {
                val foto = FotoFormulario(referenciaDeFoto(fotoRepository.nuevoId()), bytes)
                val estado = _uiState.value
                if (esPrincipal) {
                    estado.copy(fotoPrincipal = foto, procesandoFoto = false)
                } else {
                    estado.copy(galeria = (estado.galeria + foto).take(ReglasAdmin.MAX_FOTOS_GALERIA), procesandoFoto = false)
                }
            }
        }
    }

    fun quitarFotoGaleria(indice: Int) {
        val galeria = _uiState.value.galeria.filterIndexed { i, _ -> i != indice }
        _uiState.value = _uiState.value.copy(galeria = galeria)
    }

    fun guardar() {
        val estado = _uiState.value
        if (estado.cargando || estado.procesandoFoto) return
        val errorValidacion = ReglasAdmin.validarDestino(estado.formulario, estado.fotoPrincipal != null)
        val principal = estado.fotoPrincipal
        if (errorValidacion != null || principal == null) {
            _uiState.value = estado.copy(error = errorValidacion)
            return
        }

        val formulario = estado.formulario
        val campos = mapOf(
            "titulo" to formulario.titulo.trim(),
            "ubicacion" to formulario.ubicacion.trim(),
            "categoria" to formulario.categoria.trim().ifBlank { null },
            "duracion" to formulario.duracion.trim(),
            "precioDesde" to formulario.precioDesde.toInt(),
            "descripcion" to formulario.descripcion.trim(),
            "incluye" to ReglasAdmin.lineas(formulario.incluye),
            "mejorEpoca" to formulario.mejorEpoca.trim().ifBlank { null },
            "destacada" to formulario.destacada,
            // Vacías = null: el destino no aparece en el mapa (y Google Maps se busca por nombre).
            "latitud" to ReglasAdmin.numero(formulario.latitud),
            "longitud" to ReglasAdmin.numero(formulario.longitud),
            "imagen" to principal.referencia,
            "galeria" to estado.galeria.map { it.referencia },
            "orden" to orden
        )
        val anteriores = listOfNotNull(original?.imagen) + original?.galeria.orEmpty()
        val cambios = ReglasAdmin.cambiosDeFotos(anteriores, listOf(principal) + estado.galeria)

        _uiState.value = estado.copy(cargando = true, error = null)
        viewModelScope.launch {
            adminRepository.guardarDestino(destinoId, campos, cambios.nuevas, cambios.eliminadas)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(cargando = false, guardadoExitoso = true)
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(cargando = false, error = e.message)
                }
        }
    }
}
