package com.example.qrsafe.ui.qrsafe.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.LinkDao
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ACUM ACCEPTA LinkDao (ceea ce ii trimite Factory-ul)
class HistoryViewModel(private val linkDao: LinkDao) : ViewModel() {

    val history: StateFlow<List<LinkEntity>> = linkDao.getAllLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch {
            linkDao.clearAll()
        }
    }
}