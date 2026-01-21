package com.example.qrsafe.ui.qrsafe.ui.education

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.qrsafe.R
import com.example.qrsafe.ui.qrsafe.data.AcademyRepository
import com.example.qrsafe.ui.qrsafe.data.CyberGuide
import com.example.qrsafe.ui.qrsafe.data.CyberRiddle
import com.example.qrsafe.ui.qrsafe.receiver.DailyAlertReceiver
import kotlinx.coroutines.launch

@Composable
fun EducationScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { AcademyRepository() }
    val orbitronFamily = FontFamily.Default

    var selectedTab by remember { mutableIntStateOf(0) }
    val neonGlow = colorResource(id = R.color.cyber_glow)

    // --- LOGICA DE PERMISIUNI (NOU) ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                scheduleTestAlert(context)
            } else {
                Toast.makeText(context, "Avem nevoie de notificări pentru simulare!", Toast.LENGTH_LONG).show()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("CYBER ACADEMY", fontFamily = orbitronFamily, fontSize = 28.sp, color = neonGlow, modifier = Modifier.padding(bottom = 16.dp))

        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp)).padding(4.dp)) {
            TabButton("GUIDES", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            TabButton("RIDDLES", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            GuidesList()
            Spacer(modifier = Modifier.height(30.dp))
            Divider(color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Simulare Atac", color = Color.Gray, fontSize = 14.sp)
            Button(
                onClick = {
                    // VERIFICĂM DACĂ AVEM PERMISIUNEA ÎNAINTE SĂ PROGRAMĂM
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                            scheduleTestAlert(context)
                        } else {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        scheduleTestAlert(context)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Icon(Icons.Default.NotificationsActive, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("PROGRAMEAZĂ SIMULARE (10s)")
            }

        } else {
            RiddlesGame(orbitronFamily, neonGlow, repository)
        }
    }
}

// ... GuidesList rămâne la fel ...
@Composable
fun GuidesList() {
    val guides = listOf(
        CyberGuide("Phishing 101", "Nu da niciodată click pe link-uri suspecte.", Icons.Default.Email),
        CyberGuide("Parole Beton", "Folosește minim 12 caractere, simboluri și cifre.", Icons.Default.Lock),
        CyberGuide("Wi-Fi Public", "Nu intra pe aplicația de bancă pe Wi-Fi public.", Icons.Default.Wifi),
        CyberGuide("Update-uri", "Fă update la telefon imediat ce apare notificarea.", Icons.Default.SystemUpdate)
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        guides.forEach { guide ->
            var expanded by remember { mutableStateOf(false) }
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF2C5364), RoundedCornerShape(12.dp)).clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(guide.icon, contentDescription = null, tint = Color(0xFF00FF9D))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = guide.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(text = guide.description, color = Color.LightGray, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

// ... RiddlesGame rămâne la fel ...
@Composable
fun RiddlesGame(fontFamily: FontFamily, neonColor: Color, repository: AcademyRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val riddles = listOf(
        CyberRiddle("Ce este 'Phishing'?", listOf("Un sport", "Furt de date", "Un virus"), 1, "Hackerii 'pescuiesc' datele tale."),
        CyberRiddle("Care parolă e sigură?", listOf("123456", "Andrei2024", "P@nD0rA!#9"), 2, "Complexitatea contează.")
    )
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var gameFinished by remember { mutableStateOf(false) }

    if (!gameFinished) {
        val riddle = riddles.getOrElse(currentQuestionIndex) { riddles[0] }
        Column {
            Card(modifier = Modifier.fillMaxWidth().border(1.dp, neonColor, RoundedCornerShape(16.dp)), colors = CardDefaults.cardColors(containerColor = Color.Black)) {
                Text(text = "Q${currentQuestionIndex + 1}: ${riddle.question}", color = Color.White, fontSize = 20.sp, fontFamily = fontFamily, modifier = Modifier.padding(24.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            riddle.answers.forEachIndexed { index, answer ->
                val isSelected = selectedAnswer == index
                val isCorrect = index == riddle.correctAnswerIndex
                val backgroundColor by animateColorAsState(targetValue = when {
                    showResult && isCorrect -> Color(0xFF00C853)
                    showResult && isSelected && !isCorrect -> Color(0xFFD50000)
                    isSelected -> Color(0xFF2979FF)
                    else -> Color(0xFF1A1A2E)
                }, label = "ColorAnim")
                Button(onClick = { if (!showResult) selectedAnswer = index }, colors = ButtonDefaults.buttonColors(containerColor = backgroundColor), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
                    Text(answer, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                if (!showResult) {
                    if (selectedAnswer != -1) {
                        showResult = true
                        if (selectedAnswer == riddle.correctAnswerIndex) score++
                    }
                } else {
                    if (currentQuestionIndex < riddles.size - 1) {
                        showResult = false
                        selectedAnswer = -1
                        currentQuestionIndex++
                    } else {
                        gameFinished = true
                        scope.launch { repository.saveXP("user_demo_id", score * 100)
                            Toast.makeText(context, "Scor salvat în Cloud!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = neonColor)) {
                Text(if (showResult) "NEXT" else "VERIFICĂ", color = Color.Black, fontWeight = FontWeight.Bold)
            }
            if (showResult) Text(riddle.explanation, color = Color.Yellow, modifier = Modifier.padding(top = 16.dp))
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("MISIUNE COMPLETĂ!", color = neonColor, fontSize = 24.sp, fontFamily = fontFamily)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scor: $score", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = { currentQuestionIndex = 0; score = 0; showResult = false; selectedAnswer = -1; gameFinished = false }) { Text("JOACĂ DIN NOU") }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF2979FF) else Color.Transparent
    Button(onClick = onClick, modifier = modifier, colors = ButtonDefaults.buttonColors(containerColor = bgColor), shape = RoundedCornerShape(8.dp)) {
        Text(text, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// --- ACTUALIZARE FUNCȚIE ALARMĂ (MODIFICATĂ SĂ FIE MAI SIGURĂ) ---
fun scheduleTestAlert(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, DailyAlertReceiver::class.java)

    // Flag-urile sunt CRITICE pentru Android 12+
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Setăm timpul (acum + 10 secunde)
    val triggerTime = System.currentTimeMillis() + 10_000

    try {
        // Folosim set() în loc de setExact() pentru că setExact necesită o altă permisiune specială
        // set() este suficient pentru un demo și nu dă crash
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)

        Toast.makeText(context, "⚠️ Simulare programată! (Așteaptă 10s)", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Eroare la programare: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}