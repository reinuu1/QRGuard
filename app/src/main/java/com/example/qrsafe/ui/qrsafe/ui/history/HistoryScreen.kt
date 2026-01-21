package com.example.qrsafe.ui.qrsafe.ui.history

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
// --- FIX IMPORTURI ---
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import com.example.qrsafe.ui.qrsafe.ui.AppViewModelFactory

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    // FIX: Acum recunoaște getDatabase pentru că am importat AppDatabase
    val db = AppDatabase.getDatabase(context)

    val viewModel: HistoryViewModel = viewModel(factory = AppViewModelFactory(db.linkDao()))

    // FIX: Variabila corectă din ViewModel este 'history'
    val historyItems by viewModel.history.collectAsState()

    val uriHandler = LocalUriHandler.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Istoric Scanări", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (historyItems.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Șterge Tot", tint = Color.Red)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (historyItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nu ai scanat niciun link încă.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(historyItems) { item ->
                    HistoryItemCard(item) {
                        if (item.isSafe) {
                            try { uriHandler.openUri(item.url) } catch (e: Exception) { Toast.makeText(context, "Eroare link", Toast.LENGTH_SHORT).show() }
                        } else {
                            Toast.makeText(context, "⚠️ Link periculos!", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: LinkEntity, onClick: () -> Unit) {
    val isSafe = item.isSafe
    val bgColor = if (isSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val icon = if (isSafe) "✅" else "⚠️"

    Card(colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.clickable { onClick() }) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = item.url, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = item.date, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}