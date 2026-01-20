package com.example.qrsafe.ui.qrsafe.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// --- REPARATIE AICI ---
// Înainte aveai [Link::class], dar clasa ta este [LinkEntity::class]
@Database(entities = [LinkEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun linkDao(): LinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qr_safe_database"
                )
                    // Adăugăm fallbackToDestructiveMigration pentru a evita crash-uri
                    // dacă schimbi structura bazei de date în timpul testelor
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}