package com.example.qrsafe.ui.qrsafe.ui.scan

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import com.example.qrsafe.ui.qrsafe.ui.scan.CustomCaptureActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@Composable
fun ScanScreen(
    viewModel: ScanViewModel = viewModel()
) {
    var manualUrl by remember { mutableStateOf("") }
    val scanState by viewModel.scanState.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Launcher-ul pentru Camera (ZXing)
    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            manualUrl = result.contents
            viewModel.checkUrl(result.contents)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Scanare & Verificare",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 32.dp, bottom = 32.dp)
        )

        // Cardul principal pentru input
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = manualUrl,
                    onValueChange = { manualUrl = it },
                    label = { Text("Introdu URL sau Scanează") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    // Buton Verificare Manuală
                    Button(
                        onClick = { viewModel.checkUrl(manualUrl) },
                        modifier = Modifier.weight(1f).padding(end = 8.dp)
                    ) {
                        Text("Verifică")
                    }

                    // Buton Scanare QR
                    Button(
                        onClick = {
                            val options = ScanOptions()
                            options.setPrompt("Scanează codul QR")
                            options.setBeepEnabled(true)
                            options.setOrientationLocked(true)
                            // Folosim activitatea ta custom pentru camera
                            options.captureActivity = CustomCaptureActivity::class.java
                            qrLauncher.launch(options)
                        },
                        modifier = Modifier.weight(1f).padding(start = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Scanează QR")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Afișare Rezultat
        when (scanState) {
            is ScanResultState.Loading -> {
                CircularProgressIndicator()
                Text("Se verifică link-ul...", modifier = Modifier.padding(top = 8.dp))
            }
            is ScanResultState.Error -> {
                Text(
                    text = (scanState as ScanResultState.Error).message,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
            is ScanResultState.Success -> {
                val link = (scanState as ScanResultState.Success).link

                ResultCard(link) {
                    if (link.status == "SAFE") {
                        try {
                            // --- REPARAȚIA E AICI: Adăugăm https:// dacă lipsește ---
                            var finalUrl = link.url
                            if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                                finalUrl = "https://$finalUrl"
                            }
                            uriHandler.openUri(finalUrl)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Nu pot deschide browserul", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "⚠️ Link periculos! Blocat.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            else -> {
                // Stare inițială (nimic scanat)
            }
        }
    }
}

@Composable
fun ResultCard(link: LinkEntity, onClick: () -> Unit) {
    val isSafe = link.status == "SAFE"
    val bgColor = if (isSafe) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val icon = if (isSafe) "✅" else "⚠️"
    val textColor = if (isSafe) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isSafe) "LINK SIGUR (Apasă pt. deschidere)" else "LINK PERICULOS",
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = link.url, textAlign = TextAlign.Center)
        }
    }
}