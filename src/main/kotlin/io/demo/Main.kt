package io.demo

import io.demo.dto.getMostRatedNews
import io.demo.service.NewsService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.time.LocalDate


private val logger = KotlinLogging.logger {}

fun main() = runBlocking{
    val newsService = NewsService()

    try {
        val news = newsService.getNews()

        logger.info { "You have successfully received ${news.size} news items" }

        val news2 = newsService.getNews(2)

        logger.info { "You have successfully received ${news2.size} news items" }

        println("rating1 = ${news2[0].rating}")
        println("rating2 = ${news2[1].rating}")

        println("data1 = ${news2[0].publicationDate}")
        println("data2 = ${news2[1].publicationDate}")

        val period = LocalDate.of(2020, 1, 1)..LocalDate.now()
        val mostRatedNews = news2.getMostRatedNews(2, period)

        logger.info { "${mostRatedNews.size} news with the most rating has been successfully filtered" }

        println("Response text: $mostRatedNews")

        newsService.saveNews("src/main/resources/news.csv", mostRatedNews)

    } catch (e: Exception) {
        logger.error(e) { "An error occurred while processing" }
    }
}
