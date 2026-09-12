package com.example.equiposfutbolcrud

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.equiposfutbolcrud.ui.theme.EquiposFutbolCRUDTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            EquiposFutbolCRUDTheme {
                EquiposScreen()
            }
        }
    }
}

@Composable
fun EquiposScreen() {

    // Lista de equipos
    var equipos by remember {
        mutableStateOf<List<Equipo>>(emptyList())
    }

    // Campos del formulario
    var nombre by remember {
        mutableStateOf("")
    }

    var pais by remember {
        mutableStateOf("")
    }

    // Equipo que estamos editando
    var equipoEditando by remember {
        mutableStateOf<Equipo?>(null)
    }

    // Buscador
    var busqueda by remember {
        mutableStateOf("")
    }

    // Estados
    var cargando by remember {
        mutableStateOf(true)
    }

    var guardando by remember {
        mutableStateOf(false)
    }

    var eliminando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val scope = rememberCoroutineScope()

    // ============================================================
    // GET - CARGAR EQUIPOS
    // ============================================================

    LaunchedEffect(Unit) {

        try {

            equipos = RetrofitClient.api.obtenerEquipos()

        } catch (e: Exception) {

            mensaje = "Error al cargar los equipos: ${e.message}"

        } finally {

            cargando = false
        }
    }

    // ============================================================
    // BUSCAR
    // ============================================================

    val equiposFiltrados = equipos.filter { equipo ->

        equipo.nombre.contains(
            busqueda,
            ignoreCase = true
        ) || equipo.pais.contains(
            busqueda,
            ignoreCase = true
        )
    }

    Scaffold { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // ====================================================
            // TÍTULO
            // ====================================================

            Text(
                text = "⚽ Equipos de Fútbol",
                style = MaterialTheme.typography.headlineMedium
            )

            // ====================================================
            // BUSCADOR
            // ====================================================

            OutlinedTextField(
                value = busqueda,
                onValueChange = {
                    busqueda = it
                },
                label = {
                    Text("Buscar equipo o país")
                },
                placeholder = {
                    Text("Ejemplo: Real Madrid o España")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            // ====================================================
            // FORMULARIO
            // ====================================================

            Text(
                text = if (equipoEditando == null) {
                    "Agregar equipo"
                } else {
                    "Editar equipo"
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp)
            )

            // NOMBRE
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                },
                label = {
                    Text("Nombre del equipo")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // PAÍS
            OutlinedTextField(
                value = pais,
                onValueChange = {
                    pais = it
                },
                label = {
                    Text("País")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            // ====================================================
            // BOTONES AGREGAR / ACTUALIZAR
            // ====================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Button(
                    onClick = {

                        if (nombre.isBlank() || pais.isBlank()) {

                            mensaje = "Completa el nombre y el país"

                        } else {

                            scope.launch {

                                guardando = true
                                mensaje = ""

                                try {

                                    // ==================================
                                    // EDITAR
                                    // ==================================

                                    if (equipoEditando != null) {

                                        val equipoActualizado = Equipo(
                                            id = equipoEditando!!.id,
                                            nombre = nombre,
                                            pais = pais
                                        )

                                        RetrofitClient.api.actualizarEquipo(
                                            equipoEditando!!.id!!,
                                            equipoActualizado
                                        )

                                        mensaje =
                                            "Equipo actualizado correctamente"

                                    }

                                    // ==================================
                                    // AGREGAR
                                    // ==================================

                                    else {

                                        val nuevoEquipo = Equipo(
                                            nombre = nombre,
                                            pais = pais
                                        )

                                        RetrofitClient.api.crearEquipo(
                                            nuevoEquipo
                                        )

                                        mensaje =
                                            "Equipo agregado correctamente"
                                    }

                                    // Volver a cargar la lista
                                    equipos =
                                        RetrofitClient.api.obtenerEquipos()

                                    // Limpiar formulario
                                    nombre = ""
                                    pais = ""

                                    equipoEditando = null

                                } catch (e: Exception) {

                                    mensaje =
                                        "Error: ${e.message}"

                                } finally {

                                    guardando = false
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !guardando
                ) {

                    Text(
                        text = if (equipoEditando == null) {
                            "AGREGAR"
                        } else {
                            "ACTUALIZAR"
                        }
                    )
                }

                // ====================================================
                // CANCELAR EDICIÓN
                // ====================================================

                if (equipoEditando != null) {

                    Button(
                        onClick = {

                            nombre = ""
                            pais = ""
                            equipoEditando = null
                            mensaje = ""

                        },
                        modifier = Modifier.weight(1f)
                    ) {

                        Text("CANCELAR")
                    }
                }
            }

            // ====================================================
            // MENSAJE
            // ====================================================

            if (mensaje.isNotEmpty()) {

                Text(
                    text = mensaje,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // ====================================================
            // TÍTULO LISTA
            // ====================================================

            Text(
                text = "Equipos registrados (${equiposFiltrados.size})",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 8.dp
                )
            )

            // ====================================================
            // CARGANDO
            // ====================================================

            if (cargando) {

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator()
                }

            } else {

                // =================================================
                // LISTA
                // =================================================

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    items(
                        items = equiposFiltrados,
                        key = { equipo ->
                            equipo.id ?: equipo.nombre
                        }
                    ) { equipo ->

                        EquipoCard(
                            equipo = equipo,

                            // =====================================
                            // EDITAR
                            // =====================================

                            onEditar = {

                                equipoEditando = equipo

                                nombre = equipo.nombre

                                pais = equipo.pais

                                mensaje = ""
                            },

                            // =====================================
                            // ELIMINAR
                            // =====================================

                            onEliminar = {

                                scope.launch {

                                    if (equipo.id == null) {

                                        mensaje =
                                            "No se puede eliminar: el equipo no tiene ID"

                                    } else {

                                        eliminando = true
                                        mensaje = ""

                                        try {

                                            RetrofitClient.api.eliminarEquipo(
                                                equipo.id
                                            )

                                            // Actualizar lista
                                            equipos =
                                                RetrofitClient.api.obtenerEquipos()

                                            mensaje =
                                                "Equipo eliminado correctamente"

                                        } catch (e: Exception) {

                                            mensaje =
                                                "Error al eliminar: ${e.message}"

                                        } finally {

                                            eliminando = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


// =================================================================
// TARJETA DEL EQUIPO
// =================================================================

@Composable
fun EquipoCard(
    equipo: Equipo,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            // NOMBRE
            Text(
                text = equipo.nombre,
                style = MaterialTheme.typography.titleLarge
            )

            // PAÍS
            Text(
                text = "País: ${equipo.pais}",
                modifier = Modifier.padding(top = 6.dp)
            )

            // ID
            Text(
                text = "ID: ${equipo.id ?: "Sin ID"}",
                modifier = Modifier.padding(top = 4.dp)
            )

            // ====================================================
            // BOTONES EDITAR / ELIMINAR
            // ====================================================

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // EDITAR
                Button(
                    onClick = onEditar,
                    modifier = Modifier.weight(1f)
                ) {

                    Text("EDITAR")
                }

                // ELIMINAR
                Button(
                    onClick = onEliminar,
                    modifier = Modifier.weight(1f),
                    enabled = equipo.id != null
                ) {

                    Text("ELIMINAR")
                }
            }
        }
    }
}