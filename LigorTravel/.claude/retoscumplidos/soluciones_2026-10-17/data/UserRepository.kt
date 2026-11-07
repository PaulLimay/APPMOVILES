package com.example.ligortravel.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository(
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usuarios = firestore.collection("usuarios")

    suspend fun guardarPerfil(usuario: RegistroUsuario): Result<RegistroUsuario> = try {
        val id = usuario.id ?: return Result.failure(IllegalArgumentException("El usuario necesita un id"))
        usuarios.document(id).set(usuario).await()
        Result.success(usuario)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun obtenerPerfil(id: String): Result<RegistroUsuario> = try {
        val usuario = usuarios.document(id).get().await().toObject(RegistroUsuario::class.java)
        if (usuario != null) Result.success(usuario) else Result.failure(Exception("No se encontró el perfil"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
