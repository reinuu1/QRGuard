package com.example.qrsafe.ui.qrsafe.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AcademyRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // Salvăm XP-ul în Cloud (Cerința Etapa 2)
    suspend fun saveXP(userId: String, newXp: Int) {
        try {
            val data = mapOf(
                "xp" to newXp,
                "last_active" to System.currentTimeMillis()
            )
            usersCollection.document(userId).set(data).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}