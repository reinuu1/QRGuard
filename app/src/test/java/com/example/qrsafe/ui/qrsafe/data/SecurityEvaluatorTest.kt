package com.example.qrsafe.ui.qrsafe.data // Atentie la pachet, lasa-l pe cel generat de Android Studio

import org.junit.Assert.*
import org.junit.Test

class SecurityEvaluatorTest {

    @Test
    fun password_isStrong() {
        // Testam o parola slaba (prea scurta)
        assertFalse(SecurityEvaluator.isPasswordStrong("Pass!"))

        // Testam o parola slaba (fara simbol)
        assertFalse(SecurityEvaluator.isPasswordStrong("ParolaLungaDarSlaba"))

        // Testam o parola buna
        assertTrue(SecurityEvaluator.isPasswordStrong("ParolaSigura!2024"))
    }

    @Test
    fun level_calculation() {
        // Testam XP negativ
        assertEquals(0, SecurityEvaluator.calculateLevel(-10))

        // Testam Level 1 (Rookie)
        assertEquals(1, SecurityEvaluator.calculateLevel(50))

        // Testam Level 2 (Agent) - Limita de jos
        assertEquals(2, SecurityEvaluator.calculateLevel(100))

        // Testam Level 4 (Master)
        assertEquals(4, SecurityEvaluator.calculateLevel(2000))
    }
}