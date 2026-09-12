package com.example.ejercicio_4.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Post(
    @DocumentId val id: String = "",
    val texto: String = "",
    val fecha: Timestamp = Timestamp.now()
)