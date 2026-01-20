package com.example.qrsafe.ui.qrsafe.data

import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.util.regex.Pattern

data class NewsArticle(
    val title: String,
    val link: String,
    val pubDate: String,
    val description: String,
    val imageUrl: String?
)

object RssParser {
    fun parse(xml: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var insideItem = false

            var title = ""
            var link = ""
            var pubDateStr = ""
            var description = ""

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name

                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = true
                            title = ""; link = ""; pubDateStr = ""; description = ""
                        } else if (insideItem) {
                            when (tagName.lowercase()) {
                                "title" -> title = parser.nextText()
                                "link" -> link = parser.nextText()
                                "pubdate" -> pubDateStr = parser.nextText()
                                "description" -> description = parser.nextText()
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("item", ignoreCase = true)) {
                            insideItem = false

                            // --- AM SCOS FILTRUL DE DATĂ ---
                            // Acceptăm absolut tot ce vine

                            val imgUrl = extractImageUrl(description)
                            // Curățăm descrierea de HTML
                            val cleanDesc = description.replace(Regex("<.*?>"), "").trim()
                            // Luăm doar primele 150 de caractere dacă există
                            val shortDesc = if (cleanDesc.length > 150) cleanDesc.take(150) + "..." else cleanDesc

                            // Adăugăm articolul în listă
                            // Logăm titlul în consolă ca să vedem dacă funcționează
                            Log.d("RSS_DEBUG", "Gasit articol: $title")

                            articles.add(NewsArticle(title, link, pubDateStr, shortDesc, imgUrl))
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            Log.e("RSS_DEBUG", "Eroare la parsare: ${e.message}")
            e.printStackTrace()
        }
        return articles
    }

    private fun extractImageUrl(html: String): String? {
        // Căutăm imaginea în tag-ul <img src="...">
        val pattern = Pattern.compile("src=\"(.*?)\"")
        val matcher = pattern.matcher(html)
        return if (matcher.find()) matcher.group(1) else null
    }
}