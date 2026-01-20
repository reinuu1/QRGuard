package com.example.qrsafe.ui.qrsafe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.qrsafe.ui.qrsafe.ui.auth.AuthViewModel
import com.example.qrsafe.ui.qrsafe.ui.auth.LoginScreen
import com.example.qrsafe.ui.qrsafe.ui.auth.RegisterScreen
import com.example.qrsafe.ui.qrsafe.ui.home.HomeScreen
import com.example.qrsafe.ui.qrsafe.ui.theme.QRSafeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QRSafeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    // 1. Logică nouă:
    // Dacă e logat -> Home
    // Dacă NU e logat -> Welcome (Ecranul nou) -> apoi Login
    val startDestination = if (authViewModel.isUserLoggedIn()) "home" else "welcome"

    NavHost(navController = navController, startDestination = startDestination) {

        // --- RUTA NOUĂ: WELCOME ---
        composable("welcome") {
            // Importă funcția WelcomeScreen (Alt+Enter dacă e roșu)
            com.example.qrsafe.ui.qrsafe.ui.welcome.WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        // --- RUTA: LOGIN ---
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true } // Ștergem Welcome din spate
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                viewModel = authViewModel
            )
        }

        // --- RUTA: REGISTER ---
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                viewModel = authViewModel
            )
        }

        // --- RUTA: HOME ---
        composable("home") {
            HomeScreen(
                onLogout = {
                    // La logout ne întoarcem la Welcome
                    navController.navigate("welcome") {
                        popUpTo("home") { inclusive = true }
                    }
                },
                viewModel = authViewModel
            )
        }
    }
}