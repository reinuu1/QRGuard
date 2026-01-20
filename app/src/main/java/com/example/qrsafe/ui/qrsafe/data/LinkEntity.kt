package com.example.qrsafe.ui.qrsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val url: String,
    val date: String,     // Folosim numele 'date'
    val isSafe: Boolean   // Folosim Boolean
)