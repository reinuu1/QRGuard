package com.example.qrsafe.ui.qrsafe.ui.scan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.AppDatabase
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
// --- IMPORTURILE CRITICE CARE LIPSEAU ---
import com.example.qrsafe.ui.qrsafe.network.VirusTotalService
// ----------------------------------------
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScanViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)

    private val _scanState = MutableStateFlow<ScanResultState>(ScanResultState.Idle)
    val scanState = _scanState.asStateFlow()

    fun checkUrl(url: String) {
        if (url.isBlank()) return

        _scanState.value = ScanResultState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. Codăm URL-ul
                val encodedUrl = VirusTotalService.encodeUrlForVirusTotal(url)

                // 2. Apelăm API-ul
                val response = VirusTotalService.api.getUrlReport(encodedUrl)

                if (response.isSuccessful) {
                    // Aici era problema cu 'malicious' nerecunoscut.
                    // Trebuie să navigăm corect prin structura JSON.
                    val attributes = response.body()?.data?.attributes
                    val stats = attributes?.stats

                    if (stats != null) {
                        val malicious = stats.malicious
                        val harmless = stats.harmless

                        val status = if (malicious > 0) "MALICIOUS" else "SAFE"

                        val entity = LinkEntity(url = url, status = status)
                        db.linkDao().insert(entity)

                        _scanState.value = ScanResultState.Success(entity)
                    } else {
                        _scanState.value = ScanResultState.Error("Nu există date (Stats null).")
                    }
                } else {
                    _scanState.value = ScanResultState.Error("Eroare API: ${response.code()}")
                }
            } catch (e: Exception) {
                _scanState.value = ScanResultState.Error("Eroare rețea: ${e.message}")
            }
        }
    }

    fun resetState() {
        _scanState.value = ScanResultState.Idle
    }
}

sealed class ScanResultState {
    object Idle : ScanResultState()
    object Loading : ScanResultState()
    data class Success(val link: LinkEntity) : ScanResultState()
    data class Error(val message: String) : ScanResultState()
}