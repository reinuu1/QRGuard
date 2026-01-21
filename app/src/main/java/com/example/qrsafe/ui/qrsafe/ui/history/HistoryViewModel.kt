package com.example.qrsafe.ui.qrsafe.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.LinkDao
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(private val linkDao: LinkDao) : ViewModel() {

    // --- AICI ESTE CHEIA: Variabila trebuie să se numească 'allLinks' ---
    val allLinks: StateFlow<List<LinkEntity>> = linkDao.getAllLinks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        viewModelScope.launch {
            linkDao.deleteAll()
        }
    }
}