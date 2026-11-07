package com.example.ligortravel.ui.favoritos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.ligortravel.ui.components.BotonVolver

@Composable
fun FavoritosScreen(
    onVolver: ()-> Unit,
    onIrAExplorar: () -> Unit
    ){
    val Titulo = "Mis favoritos"
    val Mensaje = "Aun no tienes destinos favoritos"

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Encabezado (onVolver = onVolver)
            Text(text = Titulo)
            Text(text = Mensaje)

            Button(onClick = onIrAExplorar) {
                Text("Explorar destinos")
            }
        }
    }
}
@Composable
private fun Encabezado(onVolver: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        BotonVolver(onVolver)
    }

}