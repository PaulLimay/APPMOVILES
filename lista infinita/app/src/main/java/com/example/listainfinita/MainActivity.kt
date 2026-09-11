package com.example.listainfinita

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.listainfinita.ui.theme.ListainfinitaTheme
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ListainfinitaTheme {
                ListaPosts()
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun ListaPosts() {

    // Conexión con Firestore
    val db = remember {
        FirebaseFirestore.getInstance()
    }

    // Lista donde guardaremos los posts cargados
    val listaPosts = remember {
        mutableStateListOf<Post>()
    }

    // Último documento obtenido de Firestore
    var ultimoDocumento by remember {
        mutableStateOf<DocumentSnapshot?>(null)
    }

    // Indicador de carga
    var cargando by remember {
        mutableStateOf(false)
    }

    // Indica si todavía existen más posts
    var hayMasPosts by remember {
        mutableStateOf(true)
    }

    // Mensaje de error
    var error by remember {
        mutableStateOf<String?>(null)
    }

    // Texto del nuevo post
    var textoNuevo by remember {
        mutableStateOf("")
    }

    // FUNCIÓN PARA CARGAR POSTS DE 5 EN 5
    fun cargarNuevosPosts(
        mostrarTodos: Boolean = false
    ) {

        if (cargando) {
            return
        }

        // Si ya no hay más posts, no hacemos otra consulta
        if (!hayMasPosts) {
            return
        }

        cargando = true
        error = null

        var consulta = db.collection("posts")
            // IMPORTANTE:
            // Los posts más recientes aparecen primero
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(5)

        // Si ya cargamos una página anteriormente,
        // comenzamos después del último documento.
        if (ultimoDocumento != null) {
            consulta = consulta.startAfter(ultimoDocumento!!)
        }

        consulta.get()
            .addOnSuccessListener { resultado ->

                // Convertimos los documentos en objetos Post
                val nuevosPosts = resultado.documents.mapNotNull { documento ->
                    documento.toObject(Post::class.java)
                }

                // Agregamos los nuevos posts sin borrar los anteriores
                listaPosts.addAll(nuevosPosts)

                // Guardamos el último documento recibido
                if (resultado.documents.isNotEmpty()) {
                    ultimoDocumento = resultado.documents.last()
                }

                // Si llegaron menos de 5 significa que ya no existen más
                if (resultado.documents.size < 5) {
                    hayMasPosts = false
                }

                cargando = false

                // Si se presionó MOSTRAR MÁS,
                // seguimos cargando automáticamente las siguientes páginas.
                if (mostrarTodos && hayMasPosts) {
                    cargarNuevosPosts(true)
                }
            }
            .addOnFailureListener { excepcion ->

                error = excepcion.message
                    ?: "Error al cargar los posts"

                cargando = false
            }
    }

    // FUNCIÓN PARA MOSTRAR MENOS
    fun mostrarMenos() {

        // Borramos todos los posts cargados
        listaPosts.clear()

        // Reiniciamos la paginación
        ultimoDocumento = null
        hayMasPosts = true

        // Volvemos a cargar solamente los primeros 5
        cargarNuevosPosts()
    }

    // FUNCIÓN PARA CREAR UN NUEVO POST
    fun cargarNuevoPost() {

        // Validamos que no esté vacío
        if (textoNuevo.isBlank()) {

            error = "Escribe un texto para publicar"
            return
        }

        error = null

        // Datos que enviaremos a Firestore
        val nuevoPost = hashMapOf(
            "texto" to textoNuevo.trim(),
            "fecha" to Timestamp.now()
        )

        // Guardamos el nuevo post
        db.collection("posts")
            .add(nuevoPost)
            .addOnSuccessListener {

                // Limpiamos el campo
                textoNuevo = ""

                // Reiniciamos la paginación
                listaPosts.clear()
                ultimoDocumento = null
                hayMasPosts = true

                // Volvemos a cargar los primeros 5
                cargarNuevosPosts()
            }
            .addOnFailureListener { excepcion ->

                error = excepcion.message
                    ?: "Error al publicar el post"
            }
    }

    // Cargar los primeros 5 automáticamente
    LaunchedEffect(Unit) {
        cargarNuevosPosts()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Text(
                text = "LISTA DE POSTS",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )

            // CAMPO PARA ESCRIBIR UN NUEVO POST
            OutlinedTextField(
                value = textoNuevo,
                onValueChange = {
                    textoNuevo = it
                },
                label = {
                    Text("Escribe un nuevo post")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            // BOTÓN PARA PUBLICAR
            Button(
                onClick = {
                    cargarNuevoPost()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 8.dp
                    )
            ) {

                Text("PUBLICAR POST")
            }

            // MENSAJE DE ERROR
            if (error != null) {

                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // LISTA DE POSTS
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(listaPosts) { post ->

                    PostCard(post)
                }
            }

            // INDICADOR DE CARGA
            if (cargando) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator()

                    Text(
                        text = "Cargando...",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // MOSTRAR MÁS
            // Al presionarlo, carga automáticamente
            // todas las páginas restantes de 5 en 5.
            if (!cargando && hayMasPosts) {

                Button(
                    onClick = {

                        cargarNuevosPosts(
                            mostrarTodos = true
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                ) {

                    Text("MOSTRAR MÁS")
                }
            }

            // MOSTRAR MENOS
            // Aparece cuando ya se cargaron todos.
            if (!cargando && !hayMasPosts && listaPosts.size > 5) {

                Button(
                    onClick = {
                        mostrarMenos()
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        )
                ) {

                    Text("MOSTRAR MENOS")
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun PostCard(post: Post) {

    val fechaFormateada = post.fecha?.toDate()?.let {

        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        ).format(it)

    } ?: "Sin fecha"

    Card(
        modifier = Modifier
            .fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = post.texto,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = fechaFormateada,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
