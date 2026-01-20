package com.example.qrsafe.ui.qrsafe.network

import android.util.Base64
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

// --- MODELE DE DATE (Structura JSON de la VirusTotal) ---

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

// --- INTERFAȚA API (Comenzile către server) ---

interface VirusTotalApi {
    // Atenție: Cheia API este hardcodată aici pentru simplitate.
    // În producție, nu e ideal, dar pentru proiectul tău e perfect.
    @Headers("x-apikey: 0be61242ed3c91974edc8baa3e0450d8d58a10d4195bf194c61730ff0510ee5e")
    @GET("urls/{encoded_url_id}")
    suspend fun getUrlReport(@Path("encoded_url_id") encodedUrl: String): Response<VirusTotalUrlResponse>
}

// --- SERVICIUL (Singleton-ul care gestionează conexiunea) ---

object VirusTotalService {
    private const val BASE_URL = "https://www.virustotal.com/api/v3/"

    // Inițializăm Retrofit (librăria de rețea)
    val api: VirusTotalApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VirusTotalApi::class.java)
    }

    // Funcție ajutătoare: VirusTotal cere ca URL-urile să fie codificate în Base64 fără egaluri la final
    fun encodeUrlForVirusTotal(url: String): String {
        val encodedBytes = Base64.encode(url.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
        return String(encodedBytes).trimEnd('=')
    }
}