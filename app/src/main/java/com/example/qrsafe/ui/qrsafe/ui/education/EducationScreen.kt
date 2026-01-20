package com.example.qrsafe.ui.qrsafe.ui.education

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.qrsafe.R
import com.example.qrsafe.ui.qrsafe.data.CyberGuide
import com.example.qrsafe.ui.qrsafe.data.CyberRiddle

@Composable
fun EducationScreen() {
    // Fonturile tale Orbitron
    val orbitronFamily = FontFamily(
        Font(R.font.orbitron_medium, FontWeight.Medium),
        Font(R.font.orbitron_bold, FontWeight.Bold)
    )

    // Stare pentru Tab-uri (0 = Ghiduri, 1 = Quiz)
    var selectedTab by remember { mutableIntStateOf(0) }

    // Culori din tema ta
    val neonGlow = colorResource(id = R.color.cyber_glow)
    val cyberBg = colorResource(id = R.color.cyber_bg_start)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // TITLU
        Text(
            text = "CYBER ACADEMY",
            fontFamily = orbitronFamily,
            fontSize = 28.sp,
            color = neonGlow,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // TAB-URI CUSTOM
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            TabButton("GUIDES", selectedTab == 0, Modifier.weight(1f)) { selectedTab = 0 }
            TabButton("RIDDLES", selectedTab == 1, Modifier.weight(1f)) { selectedTab = 1 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SCHIMBARE CONȚINUT
        if (selectedTab == 0) {
            GuidesList()
        } else {
            RiddlesGame(orbitronFamily, neonGlow)
        }
    }
}

// --- COMPONENTA 1: LISTA DE GHIDURI ---
@Composable
fun GuidesList() {
    val guides = listOf(
        CyberGuide("Phishing 101", "Nu da niciodată click pe link-uri suspecte din email-uri care cer date urgente (bancă, parole).", Icons.Default.Email),
        CyberGuide("Parole Beton", "Folosește minim 12 caractere: litere Mari, mici, numere și simboluri (!@#). Nu folosi '123456'.", Icons.Default.Lock),
        CyberGuide("Wi-Fi Public", "Wi-Fi-ul de la cafenea nu e sigur. Nu intra pe aplicația de bancă fără un VPN.", Icons.Default.Wifi),
        CyberGuide("Update-uri", "Fă update la telefon imediat ce apare notificarea. Hackerii iubesc sistemele vechi.", Icons.Default.SystemUpdate),
        CyberGuide("2FA (Doi Pași)", "Activează autentificarea în 2 pași peste tot. Chiar dacă îți fură parola, nu pot intra fără codul SMS.", Icons.Default.Security)
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(guides) { guide ->
            var expanded by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF162032)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2C5364), RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(guide.icon, contentDescription = null, tint = Color(0xFF00FF9D))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = guide.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    // Animație de expandare
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = guide.description,
                            color = Color.LightGray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- COMPONENTA 2: JOCUL DE GHICITORI ---
@Composable
fun RiddlesGame(fontFamily: FontFamily, neonColor: Color) {
    val riddles = listOf(
        CyberRiddle(
            "Ce este 'Phishing'?",
            listOf("Un sport cu pești", "Furt de date prin păcăleală", "Un virus de calculator"),
            1,
            "Exact! Hackerii 'pescuiesc' datele tale cu site-uri false."
        ),
        CyberRiddle(
            "Care parolă este cea mai sigură?",
            listOf("12345678", "Andrei2024", "P@nD0rA!#99x"),
            2,
            "Corect! Combinația complexă o face aproape imposibil de spart."
        ),
        CyberRiddle(
            "Ai câștigat un iPhone într-un pop-up. Ce faci?",
            listOf("Dau click urgent!", "Închid fereastra imediat", "Completez datele de livrare"),
            1,
            "Bravo! Nimeni nu îți dă telefoane gratis pe internet. E o țeapă."
        )
    )

    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    var selectedAnswer by remember { mutableIntStateOf(-1) }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }

    if (currentQuestionIndex < riddles.size) {
        val riddle = riddles[currentQuestionIndex]

        Column {
            // Cardul cu Întrebarea
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, neonColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Text(
                    text = "Q${currentQuestionIndex + 1}: ${riddle.question}",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = fontFamily,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Variantele de Răspuns
            riddle.answers.forEachIndexed { index, answer ->
                val isSelected = selectedAnswer == index
                val isCorrect = index == riddle.correctAnswerIndex

                // Culoarea butonului (Verde=Corect, Roșu=Greșit, Albastru=Selectat)
                val backgroundColor by animateColorAsState(
                    targetValue = when {
                        showResult && isCorrect -> Color(0xFF00C853)
                        showResult && isSelected && !isCorrect -> Color(0xFFD50000)
                        isSelected -> Color(0xFF2979FF)
                        else -> Color(0xFF1A1A2E)
                    }, label = "ColorAnim"
                )

                Button(
                    onClick = { if (!showResult) selectedAnswer = index },
                    colors = ButtonDefaults.buttonColors(containerColor = backgroundColor),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(answer, fontSize = 16.sp, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Butonul de Verificare / Next
            Button(
                onClick = {
                    if (!showResult) {
                        if (selectedAnswer != -1) {
                            showResult = true
                            if (selectedAnswer == riddle.correctAnswerIndex) score++
                        }
                    } else {
                        // Reset pentru următoarea întrebare
                        showResult = false
                        selectedAnswer = -1
                        currentQuestionIndex++
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedAnswer != -1,
                colors = ButtonDefaults.buttonColors(containerColor = neonColor)
            ) {
                Text(
                    text = if (showResult) "NEXT >>" else "VERIFICĂ",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showResult) {
                Text(
                    text = riddle.explanation,
                    color = Color.Yellow,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    } else {
        // Ecran Final
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text("MISIUNE COMPLETĂ!", color = neonColor, fontSize = 24.sp, fontFamily = fontFamily)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Scor Final: $score / ${riddles.size}", color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = {
                currentQuestionIndex = 0
                score = 0
                showResult = false
                selectedAnswer = -1
            }) { Text("JOACĂ DIN NOU") }
        }
    }
}

// Buton ajutător pentru Tab-uri
@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bgColor = if (isSelected) Color(0xFF2979FF) else Color.Transparent
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold, color = Color.White)
    }
}