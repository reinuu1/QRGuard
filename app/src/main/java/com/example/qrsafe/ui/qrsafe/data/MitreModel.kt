package com.example.qrsafe.ui.qrsafe.data

import androidx.compose.ui.graphics.Color

data class MitreTechnique(
    val id: String,          // Ex: T1566
    val name: String,        // Ex: Phishing
    val tactic: String,      // Ex: Initial Access
    val description: String, // Ex: Adversaries may send phishing messages...
    val mitigation: String,  // Ex: Antivirus, User Training...
    val color: Color = Color(0xFF00B0FF) // Culoarea tacticii (Blue Team style)
)