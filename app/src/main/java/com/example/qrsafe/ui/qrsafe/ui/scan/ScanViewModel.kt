package com.example.qrsafe.ui.qrsafe.ui.scan

import android.util.Patterns
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

    private val _resultType = MutableStateFlow("TEXT")
    val resultType = _resultType.asStateFlow()

    private val _virusStatus = MutableStateFlow<String>("UNCHECKED")
    val virusStatus = _virusStatus.asStateFlow()

    private val _isEncrypted = MutableStateFlow(false)
    val isEncrypted = _isEncrypted.asStateFlow()

    private val _decryptedResult = MutableStateFlow<String?>(null)
    val decryptedResult = _decryptedResult.asStateFlow()

    fun onScanResult(result: String) {
        _scanResult.value = result
        detectType(result)
    }

    private fun detectType(content: String) {
        // 1. Curățăm conținutul de spații albe
        val cleanContent = content.trim()
        val upperContent = cleanContent.uppercase()

        when {
            // Secret Criptat
            cleanContent.startsWith("QRSAFE:") -> {
                _resultType.value = "SECRET"
                _isEncrypted.value = true
                _virusStatus.value = "SECURE_LOCKED"
            }

            // Wi-Fi
            upperContent.startsWith("WIFI:") -> {
                _resultType.value = "WIFI"
                _isEncrypted.value = false
                _decryptedResult.value = cleanContent
                _virusStatus.value = "SAFE"
                saveToHistory(cleanContent, true)
            }

            // --- FIX PENTRU VCARD ---
            // Verificăm dacă conține markerul, nu doar dacă începe cu el
            upperContent.contains("BEGIN:VCARD") && upperContent.contains("END:VCARD") -> {
                _resultType.value = "VCARD"
                _isEncrypted.value = false
                _decryptedResult.value = cleanContent
                _virusStatus.value = "SAFE"
                saveToHistory("Contact: " + extractVCardName(cleanContent), true)
            }

            // Link (URL)
            isLink(cleanContent) -> {
                _resultType.value = "URL"
                _isEncrypted.value = false
                val finalUrl = if (!cleanContent.startsWith("http")) "https://$cleanContent" else cleanContent
                _decryptedResult.value = finalUrl
                checkAndSaveLink(finalUrl)
            }

            // Text Simplu
            else -> {
                _resultType.value = "TEXT"
                _isEncrypted.value = false
                _decryptedResult.value = cleanContent
                _virusStatus.value = "SAFE"
                saveToHistory(cleanContent, true)
            }
        }
    }

    private fun isLink(text: String): Boolean {
        return text.startsWith("http") || text.startsWith("www.") || Patterns.WEB_URL.matcher(text).matches()
    }

    private fun checkAndSaveLink(url: String) {
        _virusStatus.value = "CHECKING"
        viewModelScope.launch {
            try {
                val isSafe = VirusTotalService.checkUrl(url)
                _virusStatus.value = if (isSafe) "SAFE" else "MALICIOUS"
                saveToHistory(url, isSafe)
            } catch (e: Exception) {
                _virusStatus.value = "UNKNOWN"
                saveToHistory(url, false)
            }
        }
    }

    fun attemptDecryption(password: String) {
        val rawData = _scanResult.value ?: return
        val result = CryptoManager.decrypt(rawData, password)

        if (result != null) {
            _decryptedResult.value = result
            _isEncrypted.value = false
            detectType(result) // Re-evaluăm tipul conținutului decriptat
        } else {
            _virusStatus.value = "WRONG_PASSWORD"
        }
    }

    fun resetScanner() {
        _scanResult.value = null
        _virusStatus.value = "UNCHECKED"
        _isEncrypted.value = false
        _decryptedResult.value = null
        _resultType.value = "TEXT"
    }

    private fun saveToHistory(url: String, isSafe: Boolean) {
        viewModelScope.launch {
            try {
                linkDao.insertLink(LinkEntity(id = 0, url = url, date = Date().toString(), isSafe = isSafe))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun extractVCardName(vcard: String): String {
        // Căutare mai flexibilă pentru nume (FN: sau N:)
        return vcard.lines().find { it.uppercase().startsWith("FN:") }?.substringAfter(":")
            ?: vcard.lines().find { it.uppercase().startsWith("N:") }?.substringAfter(":")?.replace(";", " ")?.trim()
            ?: "Contact Nou"
    }
}