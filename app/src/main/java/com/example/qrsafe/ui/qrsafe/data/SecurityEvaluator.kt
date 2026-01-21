package com.example.qrsafe.ui.qrsafe.data

object SecurityEvaluator {

    // Verifică puterea parolei
    fun isPasswordStrong(password: String): Boolean {
        if (password.length < 8) return false
        val specialChars = "!@#$%^&*()_+-=[]{}|;':,.<>/?"
        return password.any { it in specialChars }
    }

    // Calculează nivelul în funcție de XP
    fun calculateLevel(xp: Int): Int {
        return when {
            xp < 0 -> 0
            xp < 100 -> 1
            xp < 500 -> 2
            xp < 1000 -> 3
            else -> 4
        }
    }
}