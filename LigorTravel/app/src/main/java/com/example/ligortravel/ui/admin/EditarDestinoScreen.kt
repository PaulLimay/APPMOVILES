package com.example.ligortravel.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ligortravel.ui.components.PantallaEstado

@Composable
fun EditarDestinoScreen(
    destinoId: String?,
    onVolver: () -> Unit,
    viewModel: EditarDestinoViewModel = viewModel { EditarDestinoViewModel(destinoId) }
) {
    val uiState by viewModel.uiState.collectAsState()
    val resolver = LocalContext.current.contentResolver

    // Selector de fotos del sistema (Photo Picker): no necesita pedir permisos de almacenamiento.
    val elegirPrincipal = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoElegida(resolver, uri, esPrincipal = true)
    }
    val elegirGaleria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onFotoElegida(resolver, uri, esPrincipal = false)
    }
    val soloImagenes = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

    LaunchedEffect(uiState.guardadoExitoso) {
        if (uiState.guardadoExitoso) onVolver()
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        when {
            uiState.cargandoDatos -> PantallaEstado(onVolver) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.errorDatos != null -> PantallaEstado(onVolver) {
                Text(
                    text = uiState.errorDatos.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            else -> Column(modifier = Modifier.fillMaxSize().imePadding()) {
                EncabezadoAdmin(
                    titulo = if (destinoId == null) "Nuevo destino" else "Editar destino",
                    onVolver = onVolver
                )
                val formulario = uiState.formulario
                val cambiar = viewModel::onFormularioChange

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TituloSeccion("Foto principal")
                    val principal = uiState.fotoPrincipal
                    if (principal == null) {
                        BotonAgregarFoto(
                            texto = "Elegir foto",
                            alto = 180.dp,
                            procesando = uiState.procesandoFoto,
                            modifier = Modifier.fillMaxWidth()
                        ) { elegirPrincipal.launch(soloImagenes) }
                    } else {
                        MiniaturaFoto(foto = principal, alto = 180.dp, modifier = Modifier.fillMaxWidth())
                        TextButton(
                            onClick = { elegirPrincipal.launch(soloImagenes) },
                            enabled = !uiState.procesandoFoto
                        ) {
                            Text(if (uiState.procesandoFoto) "Procesando foto..." else "Cambiar foto principal")
                        }
                    }

                    TituloSeccion("Galería (hasta ${ReglasAdmin.MAX_FOTOS_GALERIA} fotos)")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Tres espacios del mismo ancho: fotos, luego el botón de agregar, luego vacíos.
                        for (indice in 0 until ReglasAdmin.MAX_FOTOS_GALERIA) {
                            val foto = uiState.galeria.getOrNull(indice)
                            when {
                                foto != null -> MiniaturaFoto(
                                    foto = foto,
                                    alto = 90.dp,
                                    modifier = Modifier.weight(1f),
                                    onQuitar = { viewModel.quitarFotoGaleria(indice) }
                                )
                                indice == uiState.galeria.size -> BotonAgregarFoto(
                                    texto = "Agregar",
                                    alto = 90.dp,
                                    procesando = uiState.procesandoFoto,
                                    modifier = Modifier.weight(1f)
                                ) { elegirGaleria.launch(soloImagenes) }
                                else -> Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    TituloSeccion("Datos del destino")
                    CampoAdmin("Nombre", formulario.titulo, { cambiar(formulario.copy(titulo = it)) })
                    CampoAdmin("Ubicación", formulario.ubicacion, { cambiar(formulario.copy(ubicacion = it)) })
                    CampoAdmin(
                        "Categoría",
                        formulario.categoria,
                        { cambiar(formulario.copy(categoria = it)) },
                        ayuda = "Opcional, ej. Aventura o Cultura"
                    )
                    CampoAdmin("Duración", formulario.duracion, { cambiar(formulario.copy(duracion = it)) })
                    // La calificación ya no se escribe aquí: es el promedio de las reseñas.
                    CampoAdmin(
                        "Precio desde (S/)",
                        formulario.precioDesde,
                        { cambiar(formulario.copy(precioDesde = it)) },
                        numerico = true
                    )
                    CampoAdmin(
                        "Descripción",
                        formulario.descripcion,
                        { cambiar(formulario.copy(descripcion = it)) },
                        lineas = 4,
                        ayuda = "${formulario.descripcion.length}/${ReglasAdmin.DESCRIPCION_MAX}"
                    )
                    CampoAdmin(
                        "Qué incluye",
                        formulario.incluye,
                        { cambiar(formulario.copy(incluye = it)) },
                        lineas = 3,
                        ayuda = "Uno por línea"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CampoAdmin(
                            "Latitud",
                            formulario.latitud,
                            { cambiar(formulario.copy(latitud = it)) },
                            modifier = Modifier.weight(1f),
                            ayuda = "Ej. -9.0114"
                        )
                        CampoAdmin(
                            "Longitud",
                            formulario.longitud,
                            { cambiar(formulario.copy(longitud = it)) },
                            modifier = Modifier.weight(1f),
                            ayuda = "Ej. -77.6153"
                        )
                    }
                    Text(
                        "Para el mapa. En Google Maps, mantén presionado el lugar y copia los números.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    CampoAdmin(
                        "Mejor época",
                        formulario.mejorEpoca,
                        { cambiar(formulario.copy(mejorEpoca = it)) },
                        ayuda = "Opcional"
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Destacado", color = MaterialTheme.colorScheme.onBackground, fontSize = 15.sp)
                            Text(
                                "Aparece como tarjeta grande en el Home",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = formulario.destacada,
                            onCheckedChange = { cambiar(formulario.copy(destacada = it)) }
                        )
                    }
                }

                BarraGuardar(
                    error = uiState.error,
                    cargando = uiState.cargando,
                    habilitado = !uiState.procesandoFoto,
                    onGuardar = viewModel::guardar
                )
            }
        }
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}
