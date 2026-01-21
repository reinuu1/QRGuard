package com.example.qrsafe.ui.qrsafe.ui.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.CryptoManager
import com.example.qrsafe.ui.qrsafe.data.LinkDao
import com.example.qrsafe.ui.qrsafe.data.LinkEntity
import com.example.qrsafe.ui.qrsafe.network.VirusTotalService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class ScanViewModel(private val linkDao: LinkDao) : ViewModel() {

    private val _scanResult = MutableStateFlow<String?>(null)
    val scanResult = _scanResult.asStateFlow()

    private val _virusStatus = MutableStateFlow<String>("UNCHECKED")
    val virusStatus = _virusStatus.asStateFlow()

    private val _isEncrypted = MutableStateFlow(false)
    val isEncrypted = _isEncrypted.asStateFlow()

    private val _decryptedResult = MutableStateFlow<String?>(null)
    val decryptedResult = _decryptedResult.asStateFlow()

    fun onScanResult(result: String) {
        if (result.startsWith("QRSAFE:")) {
            _scanResult.value = result
            _isEncrypted.value = true
            _virusStatus.value = "SECURE_LOCKED"
        } else {
            _scanResult.value = result
            _isEncrypted.value = false
            _decryptedResult.value = result
            checkLinkSafety(result)
            saveToHistory(result, isSafe = false)
        }
    }

    fun attemptDecryption(password: String) {
        val rawData = _scanResult.value ?: return
        val result = CryptoManager.decrypt(rawData, password)

        if (result != null) {
            _decryptedResult.value = result
            _isEncrypted.value = false
            checkLinkSafety(result)
            saveToHistory(result, isSafe = true)
        } else {
            _virusStatus.value = "WRONG_PASSWORD"
        }
    }

    fun resetScanner() {
        _scanResult.value = null
        _virusStatus.value = "UNCHECKED"
        _isEncrypted.value = false
        _decryptedResult.value = null
    }

    private fun checkLinkSafety(url: String) {
        _virusStatus.value = "CHECKING"
        viewModelScope.launch {
            try {
                val isSafe = VirusTotalService.checkUrl(url)
                _virusStatus.value = if (isSafe) "SAFE" else "MALICIOUS"
                saveToHistory(url, isSafe)
            } catch (e: Exception) {
                _virusStatus.value = "UNKNOWN"
            }
        }
    }

    private fun saveToHistory(url: String, isSafe: Boolean) {
        viewModelScope.launch {
            try {
                // Aici folosim structura corecta din Pasul 1
                linkDao.insertLink(LinkEntity(
                    id = 0,
                    url = url,
                    date = Date().toString(),
                    isSafe = isSafe
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}