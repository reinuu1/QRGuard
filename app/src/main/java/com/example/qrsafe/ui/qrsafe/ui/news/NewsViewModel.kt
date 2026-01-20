package com.example.qrsafe.ui.qrsafe.ui.news

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.ui.qrsafe.data.NewsArticle
import com.example.qrsafe.ui.qrsafe.data.RssParser
import com.example.qrsafe.ui.qrsafe.network.NewsClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {

    private val _newsState = MutableStateFlow<List<NewsArticle>>(emptyList())
    val newsState = _newsState.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        fetchNews()
    }

    private fun fetchNews() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("RSS_DEBUG", "Incepem descarcarea RSS...")

                val xmlString = NewsClient.api.getRssFeed()

                android.util.Log.d("RSS_DEBUG", "XML Descarcat (lungime: ${xmlString.length})")
                // android.util.Log.d("RSS_DEBUG", "Continut XML: $xmlString") // Decomenteaza daca vrei sa vezi tot XML-ul

                val articles = RssParser.parse(xmlString)

                android.util.Log.d("RSS_DEBUG", "Articole parsate: ${articles.size}")

                _newsState.value = articles
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("RSS_DEBUG", "EROARE FATALA: ${e.message}")
                e.printStackTrace()
                _isLoading.value = false
            }
        }
    }
}