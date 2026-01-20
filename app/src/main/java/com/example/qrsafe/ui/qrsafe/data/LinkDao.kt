package com.example.qrsafe.ui.qrsafe.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LinkDao {
    // AM REPARAT AICI: ORDER BY id DESC (nu timestamp)
    @Query("SELECT * FROM links ORDER BY id DESC")
    fun getAllLinks(): Flow<List<LinkEntity>>

    @Insert
    suspend fun insertLink(link: LinkEntity)

    @Query("DELETE FROM links")
    suspend fun clearAll()
}