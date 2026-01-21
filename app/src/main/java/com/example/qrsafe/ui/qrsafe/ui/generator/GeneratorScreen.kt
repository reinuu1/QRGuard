package com.example.qrsafe.ui.qrsafe.ui.generator

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrsafe.R
import com.example.qrsafe.ui.qrsafe.data.CryptoManager
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.io.OutputStream
import android.graphics.Color as AndroidColor

@Composable
fun GeneratorScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val neonGlow = colorResource(id = R.color.cyber_glow)
    val cyberPurple = colorResource(id = R.color.cyber_purple)

    // --- STARE PENTRU MODUL DE GENERARE ---
    // 0 = Secret/Text, 1 = Wi-Fi, 2 = Contact
    var selectedMode by remember { mutableStateOf(0) }

    // Variabile pentru SECRET
    var contentText by remember { mutableStateOf("") }
    var secretPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Variabile pentru WI-FI
    var wifiSsid by remember { mutableStateOf("") }
    var wifiPassword by remember { mutableStateOf("") }
    var wifiPassVisible by remember { mutableStateOf(false) }

    // Variabile pentru CONTACT (vCard)
    var contactName by remember { mutableStateOf("") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("QR GENERATOR PRO", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = neonGlow, modifier = Modifier.padding(bottom = 16.dp))

        // --- BARA DE SELECȚIE (TABS) ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ModeButton(icon = Icons.Default.Lock, label = "Secret", isSelected = selectedMode == 0) {
                selectedMode = 0
                qrBitmap = null
            }
            ModeButton(icon = Icons.Default.Wifi, label = "Wi-Fi", isSelected = selectedMode == 1) {
                selectedMode = 1
                qrBitmap = null
            }
            ModeButton(icon = Icons.Default.Person, label = "Contact", isSelected = selectedMode == 2) {
                selectedMode = 2
                qrBitmap = null
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- FORMULAR DINAMIC ÎN FUNCȚIE DE MOD ---
        when (selectedMode) {
            0 -> { // MOD SECRET
                Text("Criptează un mesaj sau link", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(value = contentText, onValueChange = { contentText = it }, label = "Mesaj sau Link Secret", icon = Icons.Default.Message)
                Spacer(modifier = Modifier.height(12.dp))

                CustomPasswordField(
                    value = secretPassword,
                    onValueChange = { secretPassword = it },
                    isVisible = passwordVisible,
                    onToggle = { passwordVisible = !passwordVisible },
                    label = "Parolă Protecție (Opțional)"
                )
            }
            1 -> { // MOD WI-FI
                Text("Generează QR pentru conectare Wi-Fi", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(value = wifiSsid, onValueChange = { wifiSsid = it }, label = "Nume Rețea (SSID)", icon = Icons.Default.Wifi)
                Spacer(modifier = Modifier.height(12.dp))

                CustomPasswordField(
                    value = wifiPassword,
                    onValueChange = { wifiPassword = it },
                    isVisible = wifiPassVisible,
                    onToggle = { wifiPassVisible = !wifiPassVisible },
                    label = "Parolă Wi-Fi"
                )
            }
            2 -> { // MOD CONTACT
                Text("Carte de vizită digitală (vCard)", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(value = contactName, onValueChange = { contactName = it }, label = "Nume Complet", icon = Icons.Default.Person)
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(value = contactPhone, onValueChange = { contactPhone = it }, label = "Număr Telefon", icon = Icons.Default.Phone, isNumber = true)
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(value = contactEmail, onValueChange = { contactEmail = it }, label = "Adresă Email", icon = Icons.Default.Email)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- BUTON GENERARE ---
        Button(
            onClick = {
                val dataToEncode = when (selectedMode) {
                    0 -> { // Logică Secret
                        if (contentText.isNotEmpty()) {
                            if (secretPassword.isNotEmpty()) CryptoManager.encrypt(contentText, secretPassword) else contentText
                        } else null
                    }
                    1 -> { // Logică Wi-Fi (Format Standard)
                        if (wifiSsid.isNotEmpty()) "WIFI:S:$wifiSsid;T:WPA;P:$wifiPassword;;" else null
                    }
                    2 -> { // Logică vCard (Format Standard)
                        if (contactName.isNotEmpty()) {
                            "BEGIN:VCARD\nVERSION:3.0\nN:$contactName;;;;\nFN:$contactName\nTEL;TYPE=CELL:$contactPhone\nEMAIL:$contactEmail\nEND:VCARD"
                        } else null
                    }
                    else -> null
                }

                if (dataToEncode != null) {
                    qrBitmap = generateQrBitmap(dataToEncode)
                } else {
                    Toast.makeText(context, "Completează câmpurile!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cyberPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCode2, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENEREAZĂ QR", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- PREVIEW REZULTAT ---
        if (qrBitmap != null) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(4.dp, neonGlow, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(bitmap = qrBitmap!!.asImageBitmap(), contentDescription = "QR", modifier = Modifier.fillMaxSize())
                // Iconiță centrală în funcție de mod
                val overlayIcon = when(selectedMode) {
                    0 -> if(secretPassword.isNotEmpty()) Icons.Default.Lock else null
                    1 -> Icons.Default.Wifi
                    2 -> Icons.Default.Person
                    else -> null
                }
                if (overlayIcon != null) {
                    Icon(overlayIcon, null, tint = if(selectedMode==0 && secretPassword.isNotEmpty()) Color.Red else Color.Black, modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { saveImageToGallery(context, qrBitmap!!) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))) {
                    Icon(Icons.Default.Save, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SALVEAZĂ")
                }
                Button(onClick = { shareImage(context, qrBitmap!!) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF))) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE")
                }
            }
        }
    }
}

// --- Componente UI Reutilizabile ---

@Composable
fun ModeButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) colorResource(id = R.color.cyber_glow) else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick, modifier = Modifier.background(if(isSelected) color.copy(alpha=0.2f) else Color.Transparent, androidx.compose.foundation.shape.CircleShape)) {
            Icon(icon, contentDescription = label, tint = color)
        }
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun CustomTextField(value: String, onValueChange: (String) -> Unit, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isNumber: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, tint = colorResource(id = R.color.cyber_glow)) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = if(isNumber) KeyboardOptions(keyboardType = KeyboardType.Phone) else KeyboardOptions.Default,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(id = R.color.cyber_glow),
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
fun CustomPasswordField(value: String, onValueChange: (String) -> Unit, isVisible: Boolean, onToggle: () -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = colorResource(id = R.color.cyber_glow)) },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colorResource(id = R.color.cyber_glow),
            unfocusedBorderColor = Color.Gray,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

// --- Logică Backend (QR & Save) ---

fun generateQrBitmap(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565)
        for (x in 0 until 512) {
            for (y in 0 until 512) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) { null }
}

fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "QR_SAFE_${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    var imageUri: Uri? = null
    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val contentResolver = context.contentResolver
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { contentResolver.openOutputStream(it) }
        fos?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        Toast.makeText(context, "Salvat în Galerie!", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Eroare la salvare", Toast.LENGTH_SHORT).show()
    } finally { fos?.close() }
}

fun shareImage(context: Context, bitmap: Bitmap) {
    val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "QR Safe Code", null)
    val uri = Uri.parse(path)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
    }
    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
}