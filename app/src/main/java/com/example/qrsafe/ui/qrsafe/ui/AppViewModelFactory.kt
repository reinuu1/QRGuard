package com.example.qrsafe.ui.qrsafe.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qrsafe.ui.qrsafe.data.LinkDao
import com.example.qrsafe.ui.qrsafe.ui.history.HistoryViewModel
import com.example.qrsafe.ui.qrsafe.ui.scan.ScanViewModel

class AppViewModelFactory(private val dao: LinkDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HistoryViewModel(dao) as T
        }
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}