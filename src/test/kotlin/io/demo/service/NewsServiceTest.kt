package io.demo.service

import io.demo.dto.News
import io.demo.dto.Place
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import java.io.File
import kotlin.test.*

class NewsServiceTest {

    private var newsService: NewsService = NewsService()

    @Test
    fun testGetNews() = runBlocking {
        val newsList = newsService.getNews(5)

        assertNotNull(newsList)
        assertEquals(5, newsList.size)
    }

    @Test
    fun testSaveNews() {
        val filePath = "src/test/resources/test_news.csv"

        val newsList = listOf(
            News(
                id = 1,
                title = "Test News",
                place = Place("Test Place", "Test Address"),
                description = "Test Description",
                siteUrl = "http://test.com",
                favoritesCount = 10,
                commentsCount = 5,
                dateOfPublication = 1633046400L // 2021-10-01
            )
        )

        newsService.saveNews(filePath, newsList)

        val file = File(filePath)
        assert(file.exists())
        Assertions.assertTrue(file.exists())

        val lines = file.readLines()
        assertEquals(2, lines.size)
        assertEquals("id,title,place,description,siteUrl,favoritesCount,commentsCount,publicationDate,rating", lines[0])
        assertEquals("1,Test News,Test Place,Test Description,http://test.com,10,5,2021-10-01,0.8411308951190849", lines[1])

        // удаляю созданный файл
        file.delete()
    }

    @Test
    fun testSaveNewsWithExistingFile() {
        val filePath = "src/test/resources/test_news.csv"
        val file = File(filePath)

        file.createNewFile()

        assertFailsWith<IllegalArgumentException> {
            newsService.saveNews(filePath, emptyList())
        }

        // удаляю созданный файл
        file.delete()
    }
}
