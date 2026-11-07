package com.example.ligortravel.data

import com.google.firebase.firestore.DocumentId

data class RegistroUsuario(
    @DocumentId
    val id: String? = null,
    val nombre: String = "",
    val email: String = "",
    val telefono: String = ""
)
