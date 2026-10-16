package com.example.controlaforo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlaforo.ui.theme.ControlAforoTheme
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ControlAforoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    ControlAforo(
                        modifier = Modifier.padding(padding)
                    )
                }
            }
        }
    }
}

@Composable
fun ControlAforo(modifier: Modifier = Modifier) {
    val db = remember { FirebaseFirestore.getInstance() }
    val sala = remember(db) {
        db.collection("aforo").document("sala")
    }

    var nombre by rememberSaveable { mutableStateOf("") }
    var personasAdentro by remember { mutableStateOf(0L) }
    var ultimoRegistrado by remember { mutableStateOf("Ninguno") }

    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var disponible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Escucha los cambios del documento aforo/sala.
    DisposableEffect(sala) {
        val listener = sala.addSnapshotListener { documento, excepcion ->
            cargando = false

            if (excepcion != null) {
                disponible = false
                error = "Error al leer: ${excepcion.message}"
            } else if (documento != null && documento.exists()) {
                personasAdentro =
                    documento.getLong("personasAdentro") ?: 0L

                ultimoRegistrado =
                    documento.getString("ultimoRegistrado") ?: "Ninguno"

                disponible = true
                error = null
            } else {
                disponible = false
                error = "No existe el documento aforo/sala."
            }
        }

        onDispose {
            listener.remove()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Control de Aforo",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Personas adentro: $personasAdentro",
            style = MaterialTheme.typography.titleLarge
        )

        Text(text = "Último registrado: $ultimoRegistrado")

        if (cargando) {
            CircularProgressIndicator()
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del invitado") },
            singleLine = true,
            enabled = !guardando,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = disponible && !guardando && nombre.isNotBlank(),
            onClick = {
                val nombreIngresado = nombre.trim()
                val invitado = db.collection("invitados").document()

                guardando = true
                error = null

                // Guarda el invitado y actualiza el aforo juntos.
                db.runTransaction { transaccion ->
                    val documento = transaccion.get(sala)
                    check(documento.exists()) {
                        "No existe el documento aforo/sala."
                    }

                    val actual =
                        documento.getLong("personasAdentro") ?: 0L

                    transaccion.update(
                        sala,
                        mapOf(
                            "personasAdentro" to actual + 1L,
                            "ultimoRegistrado" to nombreIngresado
                        )
                    )

                    transaccion.set(
                        invitado,
                        mapOf(
                            "nombre" to nombreIngresado,
                            "fechaIngreso" to FieldValue.serverTimestamp()
                        )
                    )

                    Unit
                }.addOnSuccessListener {
                    nombre = ""
                    guardando = false
                }.addOnFailureListener {
                    error = "No se pudo registrar: ${it.message}"
                    guardando = false
                }
            }
        ) {
            Text("Registrar e Ingresar")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                enabled = disponible && !guardando && personasAdentro > 0,
                onClick = {
                    guardando = true
                    error = null

                    db.runTransaction { transaccion ->
                        val documento = transaccion.get(sala)
                        check(documento.exists()) {
                            "No existe el documento aforo/sala."
                        }

                        val actual =
                            documento.getLong("personasAdentro") ?: 0L

                        if (actual > 0L) {
                            transaccion.update(
                                sala,
                                "personasAdentro",
                                actual - 1L
                            )
                        }

                        Unit
                    }.addOnSuccessListener {
                        guardando = false
                    }.addOnFailureListener {
                        error = "No se pudo registrar la salida: ${it.message}"
                        guardando = false
                    }
                }
            ) {
                Text("Salida (-1)")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled = disponible && !guardando,
                onClick = {
                    guardando = true
                    error = null

                    sala.update(
                        mapOf(
                            "personasAdentro" to 0L,
                            "ultimoRegistrado" to "Ninguno"
                        )
                    ).addOnSuccessListener {
                        guardando = false
                    }.addOnFailureListener {
                        error = "No se pudo reiniciar: ${it.message}"
                        guardando = false
                    }
                }
            ) {
                Text("Reiniciar")
            }
        }

        if (guardando) {
            Text("Guardando en Firebase...")
        }

        error?.let { mensaje ->
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}