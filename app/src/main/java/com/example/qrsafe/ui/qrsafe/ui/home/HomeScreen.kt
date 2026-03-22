package com.example.qrsafe.ui.qrsafe.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Create // IMPORT NOU
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.ui.auth.AuthViewModel
import com.example.qrsafe.ui.qrsafe.ui.education.EducationScreen
import com.example.qrsafe.ui.qrsafe.ui.generator.GeneratorScreen // IMPORT NOU
import com.example.qrsafe.ui.qrsafe.ui.history.HistoryScreen
import com.example.qrsafe.ui.qrsafe.ui.news.NewsScreen
import com.example.qrsafe.ui.qrsafe.ui.scan.ScanScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // 0=Scan, 1=Hist, 2=Acad, 3=Intel, 4=Create
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Safe · Qwish") },
                actions = {
                    IconButton(onClick = {
                        viewModel.logout()
                        onLogout()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Scan") },
                    label = { Text("Scan") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "History") },
                    label = { Text("List") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Academy") },
                    label = { Text("Edu") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Public, contentDescription = "News") },
                    label = { Text("Intel") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )

                // --- TAB NOU: GENERATOR ---
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Create, contentDescription = "Create") },
                    label = { Text("Gen") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ScanScreen()
                1 -> HistoryScreen()
                2 -> EducationScreen()
                3 -> NewsScreen()
                4 -> GeneratorScreen() // Aici afișăm generatorul
            }
        }
    }
}