package com.example.qrsafe.ui.qrsafe.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.ui.AppViewModelFactory
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.io.InputStream
import android.graphics.BitmapFactory

@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(db.linkDao()))

    val scanResult by viewModel.scanResult.collectAsState()
    val decryptedResult by viewModel.decryptedResult.collectAsState()
    val virusStatus by viewModel.virusStatus.collectAsState()
    val isEncrypted by viewModel.isEncrypted.collectAsState()

    var manualInput by remember { mutableStateOf("") }

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result.contents != null) { viewModel.onScanResult(result.contents) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val decodedText = decodeQRCodeFromUri(context, uri)
            if (decodedText != null) viewModel.onScanResult(decodedText)
            else Toast.makeText(context, "Nu am găsit QR", Toast.LENGTH_LONG).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val options = ScanOptions()
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            options.setPrompt("Scanează un cod QR")
            options.setCameraId(0)
            options.setBeepEnabled(false)
            options.setBarcodeImageEnabled(true)
            scanLauncher.launch(options)
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        if (scanResult == null) {
            // --- ECRAN PRINCIPAL ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("SCANARE & VERIFICARE", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(30.dp))

                // INPUT MANUAL
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Introdu un Link sau Text") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (manualInput.isNotEmpty()) viewModel.onScanResult(manualInput)
                        else Toast.makeText(context, "Scrie ceva mai întâi!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("VERIFICĂ LINK", fontWeight = FontWeight.Bold) }

                Spacer(modifier = Modifier.height(40.dp))
                HorizontalDivider(color = Color.LightGray) // Folosim HorizontalDivider sau Divider simplu
                Spacer(modifier = Modifier.height(40.dp))

                // BUTON CAMERA
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF9D))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = Color.Black, modifier = Modifier.size(50.dp))
                        Text("CAMERA", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // BUTON GALERIE
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("ALEGE DIN GALERIE", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // --- ECRAN REZULTAT ---
            ScanResultView(viewModel, isEncrypted, virusStatus, decryptedResult, context)
        }
    }
}

@Composable
fun ScanResultView(viewModel: ScanViewModel, isEncrypted: Boolean, virusStatus: String, decryptedResult: String?, context: Context) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (isEncrypted) {
            EncryptedContentDialog(onUnlock = { pass -> viewModel.attemptDecryption(pass) }, status = virusStatus, onCancel = { viewModel.resetScanner() })
        } else {
            val color = if (virusStatus == "SAFE") Color(0xFF00C853) else if (virusStatus == "MALICIOUS") Color.Red else Color.Gray
            val icon = if (virusStatus == "SAFE") Icons.Default.CheckCircle else Icons.Default.Warning

            Icon(icon, null, tint = color, modifier = Modifier.size(100.dp))
            Text(virusStatus, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth().border(1.dp, color, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E))) {
                Text(decryptedResult ?: "", modifier = Modifier.padding(16.dp), color = Color.White)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (virusStatus == "SAFE" || virusStatus == "UNCHECKED") {
                Button(
                    onClick = {
                        // FIX: Adăugăm automat https:// dacă lipsește
                        var url = decryptedResult ?: ""
                        if (!url.startsWith("http://") && !url.startsWith("https://")) {
                            url = "https://$url"
                        }
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Nu pot deschide acest link", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = color),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("OPEN LINK", color = Color.Black) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // FIX: Am schimbat culoarea textului în NEGRU ca să se vadă pe fundalul alb
            OutlinedButton(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth()) {
                Text("CLOSE SCANNER", color = Color.Black)
            }
        }
    }
}

@Composable
fun EncryptedContentDialog(onUnlock: (String) -> Unit, status: String, onCancel: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth().border(2.dp, Color(0xFF2979FF), RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, null, tint = Color(0xFF2979FF), modifier = Modifier.size(60.dp))
            Text("LOCKED CONTENT", color = Color(0xFF2979FF), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top=16.dp))
            OutlinedTextField(value = pass, onValueChange = { pass = it }, visualTransformation = PasswordVisualTransformation(), label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            if (status == "WRONG_PASSWORD") Text("Parolă Greșită!", color = Color.Red)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onUnlock(pass) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)), modifier = Modifier.fillMaxWidth()) { Text("UNLOCK") }
            TextButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}

fun decodeQRCodeFromUri(context: Context, uri: Uri): String? {
    return try {
        val stream = context.contentResolver.openInputStream(uri)
        var bitmap = BitmapFactory.decodeStream(stream) ?: return null
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = MultiFormatReader().decode(binaryBitmap, mapOf(DecodeHintType.TRY_HARDER to true))
        result.text
    } catch (e: Exception) { null }
}