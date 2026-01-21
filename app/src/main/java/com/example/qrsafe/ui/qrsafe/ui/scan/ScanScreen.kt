package com.example.qrsafe.ui.qrsafe.ui.scan

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.ui.AppViewModelFactory
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

// --- 1. OVERLAY VIZUAL (DESIGN PROFESIONAL) ---
@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    val overlayColor = Color.Black.copy(alpha = 0.6f)
    val scanAreaSize = 280.dp
    val cornerColor = Color(0xFF00FF9D) // Verde Neon
    val cornerLength = 40.dp
    val strokeWidth = 5.dp

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rectSize = scanAreaSize.toPx()

        val left = (w - rectSize) / 2
        val top = (h - rectSize) / 2
        val right = left + rectSize
        val bottom = top + rectSize

        // Tăiem gaura din mijloc
        val fullScreen = Path().apply { addRect(Rect(0f, 0f, w, h)) }
        val cutOut = Path().apply {
            addRoundRect(androidx.compose.ui.geometry.RoundRect(
                left, top, right, bottom, CornerRadius(30f, 30f)
            ))
        }
        val finalPath = Path.combine(PathOperation.Difference, fullScreen, cutOut)
        drawPath(path = finalPath, color = overlayColor)

        // Desenăm colțurile
        val stroke = Stroke(width = strokeWidth.toPx())
        val len = cornerLength.toPx()

        drawLine(cornerColor, Offset(left, top), Offset(left + len, top), stroke.width)
        drawLine(cornerColor, Offset(left, top), Offset(left, top + len), stroke.width)
        drawLine(cornerColor, Offset(right, top), Offset(right - len, top), stroke.width)
        drawLine(cornerColor, Offset(right, top), Offset(right, top + len), stroke.width)
        drawLine(cornerColor, Offset(left, bottom), Offset(left + len, bottom), stroke.width)
        drawLine(cornerColor, Offset(left, bottom), Offset(left, bottom - len), stroke.width)
        drawLine(cornerColor, Offset(right, bottom), Offset(right - len, bottom), stroke.width)
        drawLine(cornerColor, Offset(right, bottom), Offset(right, bottom - len), stroke.width)
    }
}

// --- 2. ECRANUL PRINCIPAL LOGIC ---
@Composable
fun ScanScreen() {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val db = AppDatabase.getDatabase(context)
    val viewModel: ScanViewModel = viewModel(factory = AppViewModelFactory(db.linkDao()))

    val scanResult by viewModel.scanResult.collectAsState()
    val decryptedResult by viewModel.decryptedResult.collectAsState()
    val virusStatus by viewModel.virusStatus.collectAsState()
    val isEncrypted by viewModel.isEncrypted.collectAsState()
    val resultType by viewModel.resultType.collectAsState()

    // STARE: Suntem în modul cameră sau în meniu?
    var isScanning by remember { mutableStateOf(false) }

    // STARE: Input manual
    var manualInput by remember { mutableStateOf("") }

    // STARE: Referință Cameră și Flash
    var camera by remember { mutableStateOf<Camera?>(null) }
    var isFlashOn by remember { mutableStateOf(false) }

    // Launcher Permisiuni
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) isScanning = true
            else Toast.makeText(context, "Permisiune necesară pentru cameră", Toast.LENGTH_SHORT).show()
        }
    )

    // Launcher Galerie
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val decodedText = decodeQRCodeFromUri(context, uri)
            if (decodedText != null) viewModel.onScanResult(decodedText)
            else Toast.makeText(context, "Nu am găsit QR", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (scanResult == null) {
            // --- CAZ 1: MENIUL PRINCIPAL (Text + Buton Verde) ---
            if (!isScanning) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("QR GUARD", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(40.dp))

                    // Input Manual
                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        label = { Text("Link sau Text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (manualInput.isNotEmpty()) viewModel.onScanResult(manualInput)
                            else Toast.makeText(context, "Scrie ceva!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("VERIFICĂ MANUAL", fontWeight = FontWeight.Bold) }

                    Spacer(modifier = Modifier.height(40.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(40.dp))

                    // BUTONUL VERDE MARE -> ACTIVEAZĂ CAMERA
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                isScanning = true
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.size(180.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF9D))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = Color.Black, modifier = Modifier.size(60.dp))
                            Text("SCANARE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Buton Galerie
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)),
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Image, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ALEGE DIN GALERIE", fontWeight = FontWeight.Bold)
                    }
                }
            }
            // --- CAZ 2: MODUL SCANARE (CAMERA FULL SCREEN) ---
            else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build()
                                preview.setSurfaceProvider(previewView.surfaceProvider)

                                val imageAnalysis = ImageAnalysis.Builder()
                                    .setTargetResolution(Size(1280, 720))
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()

                                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                                    // Aici ar veni logica de analiză live.
                                    // Pentru acest demo, folosim butoanele sau input-ul manual/galerie
                                    image.close()
                                }

                                try {
                                    cameraProvider.unbindAll()
                                    camera = cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                                } catch (e: Exception) { e.printStackTrace() }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    ScannerOverlay(modifier = Modifier.fillMaxSize())

                    // BUTONUL DE ÎNCHIDERE (X) - ÎNAPOI LA MENIU
                    IconButton(
                        onClick = { isScanning = false },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .size(48.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // LANTERNĂ (Fără Zoom)
                    IconButton(
                        onClick = {
                            isFlashOn = !isFlashOn
                            camera?.cameraControl?.enableTorch(isFlashOn)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 50.dp)
                            .size(60.dp)
                            .background(if(isFlashOn) Color.Yellow else Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(
                            if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if(isFlashOn) Color.Black else Color.White
                        )
                    }
                }
            }
        } else {
            // --- ECRAN REZULTAT ---
            ScanResultView(viewModel, isEncrypted, virusStatus, decryptedResult, resultType, context)
        }
    }
}

// --- ECRANUL DE REZULTAT (ACTUALIZAT PENTRU CONTACTE) ---
@Composable
fun ScanResultView(
    viewModel: ScanViewModel,
    isEncrypted: Boolean,
    virusStatus: String,
    decryptedResult: String?,
    resultType: String,
    context: Context
) {
    val clipboardManager = LocalClipboardManager.current

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {

        if (isEncrypted) {
            EncryptedContentDialog(onUnlock = { pass -> viewModel.attemptDecryption(pass) }, status = virusStatus, onCancel = { viewModel.resetScanner() })
        } else {
            val color = when(resultType) {
                "WIFI" -> Color(0xFF00B0FF)
                "VCARD" -> Color(0xFFFF9800)
                "URL" -> if (virusStatus == "SAFE") Color(0xFF00C853) else Color.Red
                else -> Color.Gray
            }
            val icon = when(resultType) {
                "WIFI" -> Icons.Default.Wifi
                "VCARD" -> Icons.Default.PersonAdd
                "URL" -> if (virusStatus == "SAFE") Icons.Default.CheckCircle else Icons.Default.Warning
                else -> Icons.Default.TextSnippet
            }
            val title = when(resultType) {
                "WIFI" -> "REȚEA WI-FI"
                "VCARD" -> "CONTACT"
                "URL" -> virusStatus
                else -> "TEXT"
            }

            Spacer(modifier = Modifier.height(30.dp))
            Icon(icon, null, tint = color, modifier = Modifier.size(80.dp))
            Text(title, color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp).border(1.dp, color, RoundedCornerShape(12.dp)), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (resultType == "VCARD") {
                        val lines = decryptedResult?.lines() ?: emptyList()
                        val name = lines.find { it.uppercase().startsWith("FN:") }?.substringAfter(":")
                            ?: lines.find { it.uppercase().startsWith("N:") }?.substringAfter(":")?.replace(";", " ")

                        val tel = lines.find { it.uppercase().startsWith("TEL") }?.substringAfter(":")
                        val email = lines.find { it.uppercase().startsWith("EMAIL") }?.substringAfter(":")

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ContactRow(Icons.Default.Person, "Nume", name)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            ContactRow(Icons.Default.Phone, "Telefon", tel)
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            ContactRow(Icons.Default.Email, "Email", email)
                        }
                    } else {
                        Text(text = decryptedResult ?: "", color = Color.Black, fontSize = 16.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // --- BUTOANE ACȚIUNE ---
            when(resultType) {
                "WIFI" -> {
                    Button(
                        onClick = {
                            context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
                            Toast.makeText(context, "Parola a fost copiată!", Toast.LENGTH_LONG).show()
                            val pass = decryptedResult?.substringAfter("P:")?.substringBefore(";") ?: ""
                            clipboardManager.setText(AnnotatedString(pass))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal=16.dp)
                    ) { Text("DESCHIDE SETĂRI WI-FI", color = Color.White) }
                }

                "VCARD" -> {
                    // Butonul care deschide agenda
                    Button(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_INSERT).apply {
                                    type = ContactsContract.Contacts.CONTENT_TYPE
                                    val lines = decryptedResult?.lines() ?: emptyList()
                                    val name = lines.find { it.uppercase().startsWith("FN:") }?.substringAfter(":")
                                    val phone = lines.find { it.uppercase().startsWith("TEL") }?.substringAfter(":")
                                    val email = lines.find { it.uppercase().startsWith("EMAIL") }?.substringAfter(":")
                                    putExtra(ContactsContract.Intents.Insert.NAME, name)
                                    putExtra(ContactsContract.Intents.Insert.PHONE, phone)
                                    putExtra(ContactsContract.Intents.Insert.EMAIL, email)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Nu pot deschide agenda", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal=16.dp)
                    ) { Text("SALVEAZĂ ÎN AGENDĂ", color = Color.White) }
                }

                "URL" -> {
                    if (virusStatus == "SAFE" || virusStatus == "UNCHECKED") {
                        Button(
                            onClick = {
                                var url = decryptedResult ?: ""
                                if (!url.startsWith("http")) url = "https://$url"
                                try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                                catch (e: Exception) { Toast.makeText(context, "Link Invalid", Toast.LENGTH_SHORT).show() }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = color),
                            modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal=16.dp)
                        ) { Text("DESCHIDE LINK", color = Color.White) }
                    }
                }

                else -> {
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(decryptedResult ?: ""))
                            Toast.makeText(context, "Copiat!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = color),
                        modifier = Modifier.fillMaxWidth().height(50.dp).padding(horizontal=16.dp)
                    ) { Text("COPIAZĂ TEXT", color = Color.White) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth().padding(horizontal=16.dp)) {
                Text("SCANARE NOUĂ", color = Color.Black)
            }
        }
    }
}

@Composable
fun EncryptedContentDialog(onUnlock: (String) -> Unit, status: String, onCancel: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    Card(modifier = Modifier.fillMaxWidth().padding(16.dp).border(2.dp, Color(0xFF2979FF), RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Lock, null, tint = Color(0xFF2979FF), modifier = Modifier.size(60.dp))
            Text("CONȚINUT SECURIZAT", color = Color(0xFF2979FF), fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top=16.dp))
            OutlinedTextField(value = pass, onValueChange = { pass = it }, visualTransformation = PasswordVisualTransformation(), label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
            if (status == "WRONG_PASSWORD") Text("Parolă Greșită!", color = Color.Red)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onUnlock(pass) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF)), modifier = Modifier.fillMaxWidth()) { Text("DEBLOCARE") }
            TextButton(onClick = onCancel) { Text("Anulează") }
        }
    }
}

@Composable
fun ContactRow(icon: ImageVector, label: String, value: String?) {
    if (!value.isNullOrEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Icon(icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 12.sp, color = Color.Gray)
                Text(value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
            }
        }
    }
}

fun decodeQRCodeFromUri(context: Context, uri: Uri): String? {
    return try {
        val stream = context.contentResolver.openInputStream(uri)
        var bitmap = android.graphics.BitmapFactory.decodeStream(stream) ?: return null
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val result = MultiFormatReader().decode(binaryBitmap, mapOf(DecodeHintType.TRY_HARDER to true))
        result.text
    } catch (e: Exception) { null }
}