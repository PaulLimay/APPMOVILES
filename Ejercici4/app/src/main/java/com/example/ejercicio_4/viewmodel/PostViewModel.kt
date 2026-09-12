package com.example.ejercicio_4.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ejercicio_4.data.PostRepository
import com.example.ejercicio_4.model.Post
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _posts = mutableStateListOf<Post>()
    val posts: List<Post> get() = _posts

    var isLoading by mutableStateOf(false)
        private set

    var hayMasPosts by mutableStateOf(true)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        cargarInicial()
    }

    fun cargarInicial() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val nuevos = repository.cargarNuevosPosts()
                _posts.clear()
                _posts.addAll(nuevos)
                hayMasPosts = nuevos.size == 5
            } catch (e: Exception) {
                error = "No se pudieron cargar los posts: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarMas() {
        if (isLoading || !hayMasPosts) return
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val nuevos = repository.cargarMasPosts()
                _posts.addAll(nuevos)
                hayMasPosts = nuevos.size == 5
            } catch (e: Exception) {
                error = "Error al cargar más posts: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}