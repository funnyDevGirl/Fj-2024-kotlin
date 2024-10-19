package io.demo

import io.demo.dto.News
import io.demo.dto.getMostRatedNews
import io.demo.service.NewsService
import io.demo.service.savePrettyNews
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import mu.KotlinLogging
import java.io.FileNotFoundException
import java.time.LocalDate
import java.util.*


private val logger = KotlinLogging.logger {}

fun main() = runBlocking{

    val newsService = NewsService()
    val filePath = "src/main/resources/news.csv"
    val filePathForSavingPrettyNews = "src/main/resources/PrettyPrintedNews.txt"

    val config: Properties = loadConfig() // Загрузка конфигурации
    val maxConcurrentRequests = config.getProperty("maxConcurrentRequests").toInt()

    val newsChannel = Channel<News>(Channel.UNLIMITED) // Канал для передачи новостей
    val numberOfWorkers = 4 // Количество Worker'ов
    val totalNewsCount = 50 // Общее число новостей для получения

    val semaphore = Semaphore(maxConcurrentRequests) // Ограничение кол-ва параллельных запросов

    val dispatcher = newFixedThreadPoolContext(numberOfWorkers, "worker-pool")

    val startTime = System.currentTimeMillis()  // Начало отсчета времени

    // Запускаю Worker'ы
    val workers = List(numberOfWorkers) { workerId ->
        launch(dispatcher) {
            var startPage = workerId

            while (startPage < totalNewsCount) {
                try {
                    semaphore.acquire()

                    val news = newsService.getNews(totalNewsCount)
                    news.forEach { newsChannel.send(it) }
                    logger.info { "Worker $workerId fetched ${news.size} items from page ${startPage + 1}." }

                } catch (e: Exception) {
                    logger.error(e) { "Worker $workerId failed to fetch news." }

                } finally {
                    semaphore.release()
                }
                startPage += numberOfWorkers
            }
        }
    }

    // Запускаю Processor
    val processor = launch {
        val newsList = mutableListOf<News>()

        for (news in newsChannel) {
            newsList.add(news)

            if (newsList.size >= totalNewsCount) break
        }
        // После сбора всех новостей, фильтрую и отправляю их в канал
        val period = LocalDate.of(2020, 1, 1)..LocalDate.now()
        val mostRatedNews = newsList.getMostRatedNews(totalNewsCount, period)

        logger.info("${mostRatedNews.size} filtered news started writing to a file")

        newsService.saveNews(filePath, mostRatedNews)
        savePrettyNews(mostRatedNews, filePathForSavingPrettyNews)
    }

    logger.debug { "Waiting workers" }
    workers.forEach { it.join() }
    logger.debug { "Workers finished" }

    logger.debug { "Closing news channel" }
    newsChannel.close() // Закрываю канал после завершения всех Worker'ов

    logger.debug { "Waiting processor" }
    processor.join() // Жду завершения Processor'а
    logger.debug { "Processor finished" }

    val endTime = System.currentTimeMillis()  // Конец отсчета времени
    val elapsedTime = endTime - startTime
    logger.info { "With the number of threads $numberOfWorkers execution time:  $elapsedTime ms" }

    logger.info { "All the news has been successfully processed and saved." }
}

fun loadConfig(): Properties {
    val properties = Properties()
    Thread.currentThread().contextClassLoader.getResourceAsStream("config.properties")?.use { inputStream ->
        properties.load(inputStream)
    } ?: throw FileNotFoundException("config.properties not found in resources.")
    return properties
}
