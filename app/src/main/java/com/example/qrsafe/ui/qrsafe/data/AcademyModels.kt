package com.example.qrsafe.ui.qrsafe.data

import androidx.compose.ui.graphics.vector.ImageVector

// Model pentru un sfat (Ghid)
data class CyberGuide(
    val title: String,
    val description: String,
    val icon: ImageVector
)

// Model pentru o întrebare (Quiz)
data class CyberRiddle(
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)