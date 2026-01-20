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
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = viewModel()
) {
    val historyItems by viewModel.historyList.collectAsState()
    val uriHandler = LocalUriHandler.current // Unealta pentru deschis browserul
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Istoric Scanări",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            if (historyItems.isNotEmpty()) {
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Șterge Tot",
                        tint = Color.Red
                    )
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
                        // LOGICA DE CLICK
                        if (item.status == "SAFE") {
                            try {
                                uriHandler.openUri(item.url)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nu pot deschide link-ul", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "⚠️ Link-ul este periculos! Nu îl deschidem.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(item: LinkEntity, onClick: () -> Unit) {
    val isSafe = item.status == "SAFE"
    val bgColor = if (isSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val icon = if (isSafe) "✅" else "⚠️"

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(item.timestamp)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.clickable { onClick() } // Facem cardul apăsabil
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = item.url,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}