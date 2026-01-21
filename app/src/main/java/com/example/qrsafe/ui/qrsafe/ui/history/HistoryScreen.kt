package com.example.qrsafe.ui.qrsafe.ui.history

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import com.example.qrsafe.ui.qrsafe.ui.AppViewModelFactory

@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: HistoryViewModel = viewModel(factory = AppViewModelFactory(db.linkDao()))
    val historyList by viewModel.allLinks.collectAsState(initial = emptyList())

    // Cyber Colors
    val bgDark = Color(0xFF0F0F13)
    val neonGreen = Color(0xFF00FF9D)
    val neonRed = Color(0xFFFF0055)
    val textLight = Color(0xFFE0E0E0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgDark)
            .padding(16.dp)
    ) {
        // --- Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, null, tint = Color(0xFF2979FF), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("SCAN LOGS", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = textLight, letterSpacing = 2.sp)
            }

            IconButton(onClick = {
                viewModel.clearHistory()
                Toast.makeText(context, "Loguri șterse!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Lista ---
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(historyList.reversed()) { link -> // Inversăm lista să vedem cele mai noi sus
                CyberHistoryCard(link, neonGreen, neonRed)
            }
        }
    }
}

@Composable
fun CyberHistoryCard(link: LinkEntity, safeColor: Color, unsafeColor: Color) {
    val isSafe = link.isSafe
    val borderColor = if (isSafe) safeColor else unsafeColor
    val icon = if (isSafe) Icons.Default.Security else Icons.Default.Warning

    // Gradient subtil pentru fundalul cardului
    val cardBrush = Brush.horizontalGradient(
        colors = listOf(
            borderColor.copy(alpha = 0.05f),
            Color(0xFF1A1A22)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .background(cardBrush)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Iconita Status
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(borderColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = borderColor)
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Detalii
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = link.url,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if(isSafe) "SECURE TARGET" else "THREAT DETECTED",
                        color = borderColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = link.date.take(16), // Doar data și ora, fără secunde lungi
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}