package com.example.qrsafe.ui.qrsafe.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "links")
data class LinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val status: String,
    val timestamp: Long = System.currentTimeMillis()
)