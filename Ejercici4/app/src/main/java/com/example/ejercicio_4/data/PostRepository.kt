package com.example.ejercicio_4.data

import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.example.ejercicio_4.model.Post
import kotlinx.coroutines.tasks.await

class PostRepository {

    private val postsCollection = Firebase.firestore.collection("posts")

    // Guarda el "cursor" de paginación: el último documento traído
    private var ultimoDocumentoVisible: DocumentSnapshot? = null

    /** Trae los primeros 5 posts, ordenados por fecha descendente. Reinicia la paginación. */
    suspend fun cargarNuevosPosts(): List<Post> {
        ultimoDocumentoVisible = null

        val snapshot = postsCollection
            .orderBy("fecha", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()

        if (snapshot.documents.isNotEmpty()) {
            ultimoDocumentoVisible = snapshot.documents.last()
        }
        return snapshot.toObjects(Post::class.java)
    }

    /** Trae los siguientes 5 posts después del último cargado. Sin repetidos. */
    suspend fun cargarMasPosts(): List<Post> {
        val cursor = ultimoDocumentoVisible ?: return emptyList()

        val snapshot = postsCollection
            .orderBy("fecha", Query.Direction.DESCENDING)
            .startAfter(cursor)
            .limit(5)
            .get()
            .await()

        if (snapshot.documents.isNotEmpty()) {
            ultimoDocumentoVisible = snapshot.documents.last()
        }
        return snapshot.toObjects(Post::class.java)
    }
}