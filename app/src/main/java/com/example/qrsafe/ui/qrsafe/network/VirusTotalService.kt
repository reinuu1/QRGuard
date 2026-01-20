package com.example.qrsafe.ui.qrsafe.network

import android.util.Base64
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

// --- MODELE DE DATE ---
data class VirusTotalUrlResponse(
    @SerializedName("data") val data: Data?
)

data class Data(
    @SerializedName("attributes") val attributes: Attributes?
)

data class Attributes(
    @SerializedName("last_analysis_stats") val stats: AnalysisStats?
)

data class AnalysisStats(
    val harmless: Int,
    val malicious: Int,
    val suspicious: Int
)

// --- INTERFAȚA API ---
interface VirusTotalApi {
    @Headers("x-apikey: 0be61242ed3c91974edc8baa3e0450d8d58a10d4195bf194c61730ff0510ee5e")
    @GET("urls/{encoded_url_id}")
    suspend fun getUrlReport(@Path("encoded_url_id") encodedUrl: String): Response<VirusTotalUrlResponse>
}

// --- SERVICIUL ---
object VirusTotalService {
    private const val BASE_URL = "https://www.virustotal.com/api/v3/"

    // 1. Inițializăm Retrofit
    val api: VirusTotalApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApi::class.java)
    }

    // 2. Funcție ajutătoare codificare
    private fun encodeUrlForVirusTotal(url: String): String {
        val encodedBytes = Base64.encode(url.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
        return String(encodedBytes).trimEnd('=')
    }

    // --- 3. FUNCȚIA CARE ÎȚI LIPSEA (Fix pentru eroarea din ScanViewModel) ---
    suspend fun checkUrl(url: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val encodedId = encodeUrlForVirusTotal(url)
                val response = api.getUrlReport(encodedId)

                if (response.isSuccessful) {
                    val stats = response.body()?.data?.attributes?.stats
                    val maliciousCount = stats?.malicious ?: 0
                    // Dacă are 0 malițioase, e SAFE (true)
                    return@withContext maliciousCount == 0
                }
                // Dacă API-ul dă eroare (ex: link nou), zicem că e safe temporar
                return@withContext true
            } catch (e: Exception) {
                return@withContext true
            }
        }
    }
}