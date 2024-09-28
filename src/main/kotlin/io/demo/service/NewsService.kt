package io.demo.service

import io.demo.dto.News
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging
import java.io.File


class NewsService {

    private val logger = KotlinLogging.logger {}

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    suspend fun getNews(count: Int = 100): List<News> {

        val pageSize = 100
        val totalPages = (count + pageSize - 1) / pageSize
        val newsList = mutableListOf<News>()

        for (page in 1..totalPages) {
            try {
                logger.info { "Fetching news for page $page with page size $pageSize" }

                val response = client.get(
                    "https://kudago.com/public-api/v1.4/news/") {
                    parameter("location", "spb")
                    parameter("page_size", pageSize)
                    parameter("page", page)
                    parameter("order_by", "-publication_date")
                    parameter("text_format", "text")
                    parameter("expand", "place")
                    parameter(
                        "fields",
                        "id,title,place,description,site_url,favorites_count,comments_count,publication_date"
                    )
                }

                val newsResponse = response.body<JsonObject>()
                val newsArray = newsResponse["results"] as? JsonArray ?: continue

                logger.info { "Fetched ${newsArray.size} news items from page $page" }

                newsArray.forEach { jsonElement ->
                    val news = Json.decodeFromJsonElement<News>(jsonElement)
                    newsList.add(news)
                }

                if (newsList.size >= count) break

            } catch (e: Exception) {
                logger.error(e) { "Failed to fetch news for page $page" }
            }
        }
        logger.info { "Returning ${newsList.size} news items" }

        return newsList.take(count)
    }

    fun saveNews(filePath: String, news: Collection<News>) {
        val file = File(filePath)
        require(!file.exists()) { "File already exists at $filePath" }

        logger.info { "Starting to save news to file $filePath" }
        logger.info { "Number of news items to save: ${news.size}" }

        try {
            file.bufferedWriter().use { writer ->
                writer.write("id,title,place,description,siteUrl,favoritesCount,commentsCount,publicationDate,rating\n")
                news.forEach {
                    writer.write(
                        "${it.id},${it.title},${it.place?.title ?: "Неизвестно"},${it.description ?: "Неизвестно"},${it.siteUrl},${it.favoritesCount},${it.commentsCount},${it.publicationDate},${it.rating}\n")
                }
            }
            logger.info { "You have successfully saved the data to file $file" }

        } catch (e: Exception) {
            logger.error(e) { "Failed to save to file $file with error: ${e.message}" }
        }
    }
}
