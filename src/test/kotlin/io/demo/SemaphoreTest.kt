package io.demo

import io.demo.dto.News
import io.demo.service.NewsService
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Semaphore
import mu.KotlinLogging
import java.util.Properties
import kotlin.test.Test
import kotlin.test.assertTrue

private val logger = KotlinLogging.logger {}

class SemaphoreTest {

    @Test
    fun testSemaphoreLimits(): Unit = runBlocking {

        val properties = Properties().apply {
            setProperty("maxConcurrentRequests", "2") // Ограничение на кол-во одновременных запросов
        }
        val mockService = mockk<NewsService>() // мокирую сервис

        // Поведение для метода getNews
        coEvery { mockService.getNews(any()) } returns listOf(
            News(1, "News One", null, null, "http://test1.com", 2, 1, 1673740800), // 2023-1-15
            News(2, "News Two", null, null, "http://test2.com", 2, 1, 1673740800) // 2023-1-15
        )

        val newsChannel = Channel<News>(Channel.UNLIMITED)
        val maxConcurrentRequests = properties.getProperty("maxConcurrentRequests").toInt()
        val semaphore = Semaphore(maxConcurrentRequests)

        // Запускаю worker'ов
        val workers = List(5) { workerId ->
            launch {
                // Произвожу задержку для визуализации влияние семафора во времени
                repeat(5) { page ->

                    semaphore.acquire()

                    try {
                        delay(100) // Имитирую задержку запроса

                        val news = mockService.getNews(page + 1) // Вызов мок-сервиса
                        logger.info { "Worker $workerId fetched items from page ${page + 1}: ${news.map { it.title }}" }

                    } finally {
                        semaphore.release()
                    }
                }
            }
        }

        val startTime = System.currentTimeMillis()
        workers.forEach { it.join() }
        val endTime = System.currentTimeMillis()

        // Проверяю, что время выполнения соответствует максимальному лимиту
        val elapsedTime = endTime - startTime
        logger.info { "With the number of threads 5 execution time:  $elapsedTime ms" }

        assertTrue { elapsedTime < 1500 } // Ожидаю, что завершится менее чем за 1500 мс

        newsChannel.close()
    }
}