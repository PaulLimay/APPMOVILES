package com.example.ligortravel.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun crearCuenta(email: String, password: String): Result<String> = try {
        val resultado = auth.createUserWithEmailAndPassword(email, password).await()
        val uid = resultado.user?.uid
        if (uid != null) Result.success(uid) else Result.failure(Exception("No se pudo crear la cuenta"))
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    suspend fun iniciarSesion(email: String, password: String): Result<String> = try {
        val resultado = auth.signInWithEmailAndPassword(email, password).await()
        val uid = resultado.user?.uid
        if (uid != null) Result.success(uid) else Result.failure(Exception("No se pudo iniciar sesión"))
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }
    suspend fun actualizarPassword(passwordActual: String, passwordNueva: String): Result<Unit> = try {
        val usuario = auth.currentUser ?: return Result.failure(Exception("No hay sesión activa"))
        val email = usuario.email ?: return Result.failure(Exception("..."))

        val credential = EmailAuthProvider.getCredential(email, passwordActual)
        usuario.reauthenticate(credential).await()   // paso 1: verifica la contraseña actual
        usuario.updatePassword(passwordNueva).await() // paso 2: recién acá cambia la contraseña

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception(mensajeError(e)))
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    // Firebase Auth lanza excepciones con mensajes en inglés; los traducimos
    // a mensajes que un usuario final pueda entender.
    private fun mensajeError(e: Exception): String = when (e) {
        is FirebaseAuthInvalidUserException -> when (e.errorCode) {
            "ERROR_USER_NOT_FOUND" -> "No existe una cuenta con este correo"
            "ERROR_USER_DISABLED" -> "Esta cuenta fue deshabilitada"
            else -> "No se encontró la cuenta"
        }
        is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
            "ERROR_INVALID_EMAIL" -> "El correo no tiene un formato válido"
            "ERROR_WRONG_PASSWORD" -> "La contraseña es incorrecta"
            else -> "El correo o la contraseña son incorrectos"
        }
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta registrada con este correo"
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil, usa al menos 6 caracteres"
        is FirebaseNetworkException -> "No hay conexión a internet, verifica tu red"
        is FirebaseTooManyRequestsException -> "Demasiados intentos fallidos, espera un momento y vuelve a intentar"
        else -> "Ocurrió un error inesperado, intenta de nuevo"
    }
}
