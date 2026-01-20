package com.example.qrsafe.ui.qrsafe.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Public // Iconița pentru Știri
import androidx.compose.material.icons.filled.School // Iconița pentru Academy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.ui.auth.AuthViewModel
import com.example.qrsafe.ui.qrsafe.ui.education.EducationScreen // Asigură-te că ai creat acest fișier anterior
import com.example.qrsafe.ui.qrsafe.ui.history.HistoryScreen
import com.example.qrsafe.ui.qrsafe.ui.news.NewsScreen // Asigură-te că ai creat acest fișier anterior
import com.example.qrsafe.ui.qrsafe.ui.scan.ScanScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Starea pentru tab-ul selectat
    // 0 = Scan, 1 = History, 2 = Academy, 3 = Intel (News)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Guard") },
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
                // TAB 1: SCANARE
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Scan") },
                    label = { Text("Scanare") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )

                // TAB 2: ISTORIC
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Istoric") },
                    label = { Text("Istoric") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )

                // TAB 3: ACADEMY (Educație & Quiz)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Academy") },
                    label = { Text("Academy") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )

                // TAB 4: INTEL (Știri DNSC / HackerNews)
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Public, contentDescription = "News") },
                    label = { Text("Intel") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
            }
        }
    ) { innerPadding ->
        // Aici schimbăm ecranele în funcție de tab-ul apăsat
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                0 -> ScanScreen()
                1 -> HistoryScreen()
                2 -> EducationScreen()
                3 -> NewsScreen()
            }
        }
    }
}