package com.example.qrsafe.ui.qrsafe.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = AppDatabase.getInstance(application)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState = _authState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(auth.currentUser != null)
    val isLoggedIn = _isLoggedIn.asStateFlow()

    init {
        // Ascultăm schimbările de stare ale Firebase (logat/delogat)
        auth.addAuthStateListener { firebaseAuth ->
            _isLoggedIn.value = firebaseAuth.currentUser != null
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Eroare la autentificare")
            }
    }

    fun register(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                _authState.value = AuthState.Success
            }
            .addOnFailureListener { e ->
                _authState.value = AuthState.Error(e.message ?: "Eroare la înregistrare")
            }
    }

    fun logout() {
        viewModelScope.launch {
            auth.signOut()
            db.linkDao().clearAll()
            _authState.value = AuthState.Idle
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}