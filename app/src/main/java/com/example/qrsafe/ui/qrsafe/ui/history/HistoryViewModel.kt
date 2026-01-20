package com.example.qrsafe.ui.qrsafe.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    // Lista de link-uri pe care o va observa UI-ul
    private val _historyList = MutableStateFlow<List<LinkEntity>>(emptyList())
    val historyList = _historyList.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            // Luăm datele din bază (sortate după timp, cum am definit în DAO)
            _historyList.value = db.linkDao().getAllLinks()
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.linkDao().clearAll()
            loadHistory() // Reîncărcăm lista (care va fi goală)
        }
    }
}