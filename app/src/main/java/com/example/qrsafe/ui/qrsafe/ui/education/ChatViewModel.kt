package com.example.qrsafe.ui.qrsafe.ui.education

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrsafe.BuildConfig
import com.example.qrsafe.ui.qrsafe.data.UIChatMessage
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    companion object {
        private const val TAG = "QWISH_AI"

        private val AGENT_SYSTEM_PROMPT = """
            You are Qwish AI, the in-app assistant for QR Safe / Qwish Academy — an education app about QR safety, phishing, and secure habits.
            Answer clearly and helpfully in the same language the user writes in (Romanian, English, etc.).
            You may answer general questions too (study, tech, life advice), not only security — but when relevant, tie advice to safe QR usage, link checking, and avoiding scams.
            Never provide instructions for harming people, illegal activity, or bypassing security for malicious purposes.
            If you are unsure, say so briefly.
        """.trimIndent()

        private val WELCOME =
            "Salut! Sunt Qwish AI — poți întreba orice: securitate QR, phishing, sau orice altceva te ajută să înveți. Cu ce începem?"
    }

    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    private val generativeModel: GenerativeModel? =
        if (apiKey.isNotBlank()) {
            GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey,
                systemInstruction = content { text(AGENT_SYSTEM_PROMPT) },
            )
        } else {
            null
        }

    private var chatSession: Chat? = generativeModel?.startChat()

    private val _messages = MutableStateFlow(
        listOf(UIChatMessage(WELCOME, isUser = false)),
    )
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hasApiKey: Boolean get() = apiKey.isNotBlank()

    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val currentList = _messages.value.toMutableList()
        currentList.add(UIChatMessage(userText, isUser = true))
        _messages.value = currentList

        if (generativeModel == null || chatSession == null) {
            currentList.add(
                UIChatMessage(
                    "Adaugă GEMINI_API_KEY în fișierul local.properties din rădăcina proiectului, apoi reconstruiește aplicația.",
                    isUser = false,
                ),
            )
            _messages.value = currentList
            return
        }
        _isLoading.value = true

        viewModelScope.launch {
            try {
                Log.d(TAG, "user: $userText")
                val response = chatSession!!.sendMessage(userText)
                val botReply = response.text?.trim().orEmpty().ifBlank {
                    "Nu am putut genera un răspuns text. Încearcă din nou."
                }
                Log.d(TAG, "model: $botReply")

                val updated = _messages.value.toMutableList()
                updated.add(UIChatMessage(botReply, isUser = false))
                _messages.value = updated
            } catch (e: Exception) {
                Log.e(TAG, "chat error", e)
                val err = _messages.value.toMutableList()
                err.add(
                    UIChatMessage(
                        "Nu am putut răspunde: ${e.message ?: "eroare necunoscută"}",
                        isUser = false,
                    ),
                )
                _messages.value = err
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearConversation() {
        chatSession = generativeModel?.startChat()
        _messages.value = listOf(UIChatMessage(WELCOME, isUser = false))
    }
}
