package com.example.ligortravel.ui.favoritos

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale

@Composable
fun FavoritosScreen(onVolver: ()-> Unit){
    val Titulo = "Mis favoritos"
    val Mensaje = "Aun no tienes destinos favoritos"

    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
    ) {
        Text(Titulo)
        Column() {
            Text(Mensaje)
        }

    }

}
