package com.example.qrsafe.ui.qrsafe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    @Insert
    suspend fun insertLink(link: LinkEntity)

    @Query("SELECT * FROM links ORDER BY id DESC")
    fun getAllLinks(): Flow<List<LinkEntity>>

    // --- NUMELE CORECT ESTE 'deleteAll' ---
    @Query("DELETE FROM links")
    suspend fun deleteAll()
}