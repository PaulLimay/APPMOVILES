package com.example.ejercicio_4.iu

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import com.example.ejercicio_4.model.Post
import com.example.ejercicio_4.viewmodel.PostViewModel
import java.util.Locale

@Composable
fun PostListScreen(viewModel: PostViewModel = viewModel()) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Muestra el error solo si existe (ej. sin conexión o permisos de Firestore)
        viewModel.error?.let { mensaje ->
            Text(
                text = mensaje,
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            items(viewModel.posts, key = { it.id }) { post ->
                PostItem(post)
                HorizontalDivider()
            }

            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        viewModel.isLoading -> CircularProgressIndicator()
                        viewModel.hayMasPosts -> Button(onClick = { viewModel.cargarMas() }) {
                            Text("Cargar más")
                        }
                        else -> Text("No hay más posts", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(post: Post) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(post.texto, style = MaterialTheme.typography.bodyLarge)
        Text(
            text = formatearFecha(post.fecha),
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private fun formatearFecha(timestamp: Timestamp): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}