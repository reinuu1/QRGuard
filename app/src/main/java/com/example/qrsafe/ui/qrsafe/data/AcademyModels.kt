package com.example.qrsafe.ui.qrsafe.data

import androidx.compose.ui.graphics.vector.ImageVector

// Modelele tale originale
data class CyberGuide(
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class CyberRiddle(
    val question: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String
)

// Model pentru Firebase (Stocare progres)
data class UserProgress(
    val xp: Int = 0,
    val lastActive: Long = 0
)