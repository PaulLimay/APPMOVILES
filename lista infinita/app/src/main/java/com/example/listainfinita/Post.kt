package com.example.listainfinita

import com.google.firebase.Timestamp

data class Post(
    val texto: String = "",
    val fecha: Timestamp? = null
)