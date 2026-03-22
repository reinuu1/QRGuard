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
import com.example.qrsafe.ui.qrsafe.data.CyberRiddle
import com.example.qrsafe.ui.qrsafe.receiver.DailyAlertReceiver
import kotlinx.coroutines.launch

// --- MODEL DATE MITRE (Local pentru a evita erori de import) ---
data class MitreTechnique(
    val id: String,
    val name: String,
    val tactic: String,
    val description: String,
    val mitigation: String,
    val color: Color
)

@Composable
fun EducationScreen() {
    val context = LocalContext.current
    val repository = remember { AcademyRepository() }
    val orbitronFamily = FontFamily.Default

    // Stare pentru Tab-ul selectat (0=Mitre, 1=Riddles, 2=Chat)
    var selectedTab by remember { mutableIntStateOf(0) }
    val neonGlow = colorResource(id = R.color.cyber_glow)

    // --- LOGICA DE PERMISIUNI ---
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
            .padding(16.dp),
    ) {
        Text(
            text = "QWISH ACADEMY",
            fontFamily = orbitronFamily,
            fontSize = 28.sp,
            color = neonGlow,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Text(
            text = "QR Safe · educație & antrenament",
            color = Color.Gray,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // --- ZONA DE TAB-URI (Acum sunt 3) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            TabButton("MITRE", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            TabButton("RIDDLES", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
            TabButton("QWISH AI", selectedTab == 2, Modifier.weight(1f)) { selectedTab = 2 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                MitreLibraryList()
                Spacer(modifier = Modifier.height(30.dp))
                HorizontalDivider(color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Antrenament Defensiv", color = Color.Gray, fontSize = 14.sp)
                Button(
                    onClick = {
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
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Icon(Icons.Default.NotificationsActive, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PROGRAMEAZĂ SIMULARE (10s)")
                }
            }
            1 -> Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
            ) {
                RiddlesGame(orbitronFamily, neonGlow, repository)
            }
            2 -> ChatScreen(modifier = Modifier.weight(1f))
        }
    }
}

// --- NOUA LISTĂ MITRE (PROFESIONALĂ) ---
@Composable
fun MitreLibraryList() {
    val techniques = listOf(
        MitreTechnique(
            id = "T1566",
            name = "Phishing",
            tactic = "Initial Access",
            description = "Adversarii trimite mesaje frauduloase pentru a păcăli utilizatorii să dezvăluie date sensibile sau să instaleze malware.",
            mitigation = "M1021: User Training. Nu deschide atașamente sau link-uri din surse necunoscute.",
            color = Color(0xFFFF5252) // Roșu
        ),
        MitreTechnique(
            id = "T1003",
            name = "OS Credential Dumping",
            tactic = "Credential Access",
            description = "Hackerii încearcă să extragă hash-uri de parole din memoria sistemului (ex: LSASS pe Windows) pentru a fura conturi.",
            mitigation = "M1043: Credential Protection. Limitează drepturile de administrator local.",
            color = Color(0xFFE040FB) // Mov
        ),
        MitreTechnique(
            id = "T1204",
            name = "User Execution",
            tactic = "Execution",
            description = "Atacatorul se bazează pe faptul că utilizatorul va da click pe un fișier malițios (ex: un PDF fals).",
            mitigation = "M1050: Exploit Protection. Folosește extensii de browser care blochează scripturi malițioase.",
            color = Color(0xFFFFAB00) // Portocaliu
        ),
        MitreTechnique(
            id = "T1078",
            name = "Valid Accounts",
            tactic = "Defense Evasion",
            description = "Folosirea conturilor furate (username/parolă validă) pentru a trece neobservat prin sistemele de securitate.",
            mitigation = "M1032: Multi-Factor Authentication (MFA). Activează 2FA peste tot.",
            color = Color(0xFF00E676) // Verde
        ),
        MitreTechnique(
            id = "T1059",
            name = "Command Interpreter",
            tactic = "Execution",
            description = "Abuzarea de PowerShell sau CMD pentru a rula comenzi malițioase fără a instala programe noi.",
            mitigation = "M1038: Execution Prevention. Restricționează rularea scripturilor PowerShell.",
            color = Color(0xFF2979FF) // Albastru
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Header
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF212121)),
            modifier = Modifier.fillMaxWidth().border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = Color(0xFF00FF9D))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("MITRE ATT&CK® Matrix", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Bază de date oficială a tacticilor adversare", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        techniques.forEach { item ->
            MitreCard(item)
        }
    }
}

@Composable
fun MitreCard(item: MitreTechnique) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, item.color.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        color = item.color.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Text(
                            text = item.id,
                            color = item.color,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(text = item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("TACTICĂ:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(item.tactic, color = item.color, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("DESCRIERE:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(item.description, color = Color.LightGray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2C3E50), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, null, tint = Color(0xFF64B5F6), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(item.mitigation, color = Color(0xFF64B5F6), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

// --- RIDDLES GAME (Rămâne neschimbat) ---
@Composable
fun RiddlesGame(fontFamily: FontFamily, neonColor: Color, repository: AcademyRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val riddles = listOf(
        CyberRiddle("Ce este 'Phishing'?", listOf("Un sport", "Furt de date", "Un virus"), 1, "Hackerii 'pescuiesc' datele tale."),
        CyberRiddle("Care parolă e sigură?", listOf("123456", "Andrei2024", "P@nD0rA!#9"), 2, "Complexitatea contează."),
        CyberRiddle("Ai câștigat un iPhone. Ce faci?", listOf("Click!", "Închid", "Dau datele"), 1, "E o țeapă clasică.")
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

// --- ALARMĂ ---
fun scheduleTestAlert(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, DailyAlertReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val triggerTime = System.currentTimeMillis() + 10_000
    try {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        Toast.makeText(context, "⚠️ Simulare programată! (Așteaptă 10s)", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Eroare: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}