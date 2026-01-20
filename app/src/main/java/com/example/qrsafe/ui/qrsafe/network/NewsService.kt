package com.example.qrsafe.ui.qrsafe.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET

interface HackerNewsApi {
    // Schimbăm sursa: Cerem direct de la TheHackerNews, forțând formatul RSS
    @GET("feeds/posts/default?alt=rss")
    suspend fun getRssFeed(): String
}

object NewsClient {
    // Noua adresă de bază (Sursa directă)
    private const val BASE_URL = "https://thehackernews.com/"

    // Client HTTP "Deghizat" în browser real
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Connection", "keep-alive")
                .header("Upgrade-Insecure-Requests", "1")
                .build()
            chain.proceed(request)
        }
        .build()

    val api: HackerNewsApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(HackerNewsApi::class.java)
    }
}