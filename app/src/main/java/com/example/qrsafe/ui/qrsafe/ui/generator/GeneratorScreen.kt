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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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

    var contentText by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // --- STARE NOUĂ PENTRU VIZIBILITATE PAROLĂ ---
    var passwordVisible by remember { mutableStateOf(false) }

    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titlu
        Text(
            text = "SECURE GENERATOR",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = neonGlow,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- INPUT MESAJ / LINK ---
        OutlinedTextField(
            value = contentText,
            onValueChange = { contentText = it },
            label = { Text("Link sau Mesaj Secret") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonGlow,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = neonGlow,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- INPUT PAROLĂ (MODIFICAT CU OCHI) ---
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Parolă (Opțional)") },
            // AICI E LOGICA: Dacă e vizibil -> Text simplu, Altfel -> Steluțe
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                // AICI E BUTONUL OCHI
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ascunde parola" else "Arată parola"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = neonGlow)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = neonGlow,
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedLabelColor = neonGlow,
                unfocusedLabelColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // BUTON GENERARE
        Button(
            onClick = {
                if (contentText.isNotEmpty()) {
                    val finalData = if (password.isNotEmpty()) {
                        CryptoManager.encrypt(contentText, password)
                    } else {
                        contentText
                    }
                    qrBitmap = generateQrBitmap(finalData)
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = cyberPurple),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.QrCode2, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENEREAZĂ CODUL", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // REZULTAT
        if (qrBitmap != null) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(4.dp, neonGlow, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap!!.asImageBitmap(),
                    contentDescription = "QR",
                    modifier = Modifier.fillMaxSize()
                )
                // Dacă e criptat, punem un lacăt roșu peste el
                if (password.isNotEmpty()) {
                    Icon(Icons.Default.Lock, contentDescription = "Secured", tint = Color(0xFFD50000), modifier = Modifier.size(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Butoane Save & Share
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = { saveImageToGallery(context, qrBitmap!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SALVEAZĂ")
                }

                Button(
                    onClick = { shareImage(context, qrBitmap!!) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2979FF))
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHARE")
                }
            }
        }
    }
}

// --- Logică Tehnică ---

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
    } finally {
        fos?.close()
    }
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