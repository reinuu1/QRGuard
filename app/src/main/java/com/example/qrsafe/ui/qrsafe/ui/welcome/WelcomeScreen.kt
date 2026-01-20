package com.example.qrsafe.ui.qrsafe.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// --- IMPORT CRITIC PENTRU ALPHA ---
import androidx.compose.ui.draw.alpha
// ----------------------------------
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.qrsafe.R

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit
) {
    // 1. Configurăm Fonturile Orbitron
    val orbitronFamily = FontFamily(
        Font(R.font.orbitron_medium, FontWeight.Medium),
        Font(R.font.orbitron_bold, FontWeight.Bold)
    )

    // 2. Încărcăm Animațiile Lottie
    val particleComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.particle))
    val introComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.intro))

    // 3. Controlul Animației Intro (Scutul)
    val introProgress by animateLottieCompositionAsState(
        composition = introComposition,
        iterations = 1 // O singură dată
    )

    // Culori din colors.xml
    val neonGlow = colorResource(id = R.color.cyber_glow)
    val cyberPurple = colorResource(id = R.color.cyber_purple)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colorResource(id = R.color.cyber_bg_start),
                        colorResource(id = R.color.cyber_bg_end)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // --- LAYER 1: FUNDAL PARTICULE (particle.json) ---
        LottieAnimation(
            composition = particleComposition,
            iterations = LottieConstants.IterateForever,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.6f)
        )

        // --- LAYER 2: CONȚINUTUL PRINCIPAL ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {

            // LOGO ANIMAT (Scutul)
            Box(modifier = Modifier.size(280.dp)) {
                LottieAnimation(
                    composition = introComposition,
                    progress = { introProgress },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TITLU (QR SAFE)
            Text(
                text = "QR GUARD",
                fontFamily = orbitronFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = neonGlow,
                letterSpacing = 2.sp
            )

            // SUBTITLU - Font Orbitron Medium
            Text(
                text = "SECURE SCANNING PROTOCOL",
                fontFamily = orbitronFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color.LightGray,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(100.dp))

            // BUTONUL START - Stil Cyberpunk
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cyberPurple,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 10.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = "INITIALIZE SYSTEM",
                    fontFamily = orbitronFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}