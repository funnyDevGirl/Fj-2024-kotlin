package io.demo.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.abs

class NewsTest {

    @Test
    fun testPublicationDateCalculation() {
        val testDate = LocalDate.of(2023, 1, 15)
        val millis = testDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

        println("2023-01-15 in milliseconds is: $millis")

        val news = News(1, "Test News", null, null, "http://test.com", 10, 0, millis)

        val publicationDate = news.publicationDate
        println("Publication date: $publicationDate")
    }

    @Test
    fun testGetMostRatedNewsWithNoNewsInPeriod() {
        val emptyList = emptyList<News>()
        val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
        val result = emptyList.getMostRatedNews(5, period)
        assertEquals(emptyList<News>(), result)
    }

    @Test
    fun testGetMostRatedNewsWithSingleNewsInPeriod() {
        val newsList = listOf(
            News(1, "Single News", null, null, "http://test.com", 10, 0, 1673740800) // 2023-1-15
        )
        val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
        val result = newsList.getMostRatedNews(5, period)
        assertEquals(newsList, result)
    }

    @Test
    fun testGetMostRatedNewsWithMultipleNewsInPeriod() {
        val newsList = listOf(
            News(1, "Low Rated News", null, null, "http://test1.com", 1, 0, 1673740800), // 2023-1-15
            News(2, "High Rated News", null, null, "http://test2.com", 10, 1, 1673827200), // 2023-1-16
            News(3, "Medium Rated News", null, null, "http://test3.com", 5, 0, 1673913600) // 2023-1-17
        )
        val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
        val result = newsList.getMostRatedNews(2, period)

        assertEquals(listOf(newsList[1], newsList[2]), result)
    }

    @Test
    fun testGetMostRatedNewsWithRankedNewsSharingTheSameRating() {
        val newsList = listOf(
            News(1, "News One", null, null, "http://test1.com", 2, 1, 1673740800), // 2023-1-15
            News(2, "News Two", null, null, "http://test2.com", 2, 1, 1673740800) // 2023-1-15
        )
        val period = LocalDate.of(2023, 1, 15)..LocalDate.of(2023, 1, 16)
        val result = newsList.getMostRatedNews(2, period)

        assertEquals(newsList, result) // Оба должны вернуться, т.к. имеют одинаковый рейтинг
    }

    @Test
    fun testGetMostRatedNewsWithExceedingCountLimit() {
        val newsList = listOf(
            News(1, "News A", null, null, "http://test.com", 20, 0, 1673740800), // 2023-1-15
            News(2, "News B", null, null, "http://test.com", 15, 0, 1673827200), // 2023-1-16
            News(3, "News C", null, null, "http://test.com", 10, 0, 1673913600) // 2023-1-17
        )

        val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
        val result = newsList.getMostRatedNews(2, period)

        assertEquals(listOf(newsList[0], newsList[1]), result) // Только верхние 2 новости
    }

    @Test
    fun testGetMostRatedNewsWithNoNewsInValidRange() {
        val newsList = listOf(
            News(1,"Old News", null, null, "http://test.com", 1, 1, 1672444800) // 2022-12-31
        )

        val period = LocalDate.of(2023, 1, 1)..LocalDate.of(2023, 1, 31)
        val result = newsList.getMostRatedNews(5, period)

        assertEquals(emptyList<News>(), result) // Нет новостей в этом диапазоне
    }

    @Test
    fun testGetMostRatedNewsReturnsSortedListByRatingWithinDateRange() {
        val newsList = listOf(
            News(1, "Old News", null, null, "http://test1.com", 100, 10, 1627776000), // Дата 2021-08-01
            News(2, "Newer News", null, null, "http://test2.com", 200, 5, 1633046400), // Дата 2021-10-01
            News(3, "Future News", null, null, "http://test3.com", 50, 20, 1672531200) // Дата 2023-01-01
        )

        val period = LocalDate.of(2021, 8, 1)..LocalDate.of(2021, 12, 31)
        val result = newsList.getMostRatedNews(2, period)

        assertEquals(2, result.size)
        assertEquals("Newer News", result[0].title)
        assertEquals("Old News", result[1].title)
    }

    @Test
    fun testGetMostRatedNewsReturnsEmptyListIfAllNewsAreOutsideTheDateRange() {
        val newsList = listOf(
            News(1, "Future News", null, null, "http://test.com", 100, 10, 167253119999999)
        )

        val period = LocalDate.of(2021, 8, 1)..LocalDate.of(2021, 12, 31)
        val result = newsList.getMostRatedNews(1, period)

        assertEquals(0, result.size)
    }

    @Test
    fun testGetMostRatedNewsReturnsEmptyList() {
        val result = emptyList<News>().getMostRatedNews(1, LocalDate.of(2021, 8, 1)..LocalDate.of(2021, 12, 31))

        assertEquals(0, result.size)
    }

    @Test
    fun testPublicationDate() {
        val news = News(
            id = 1,
            title = "PublicationDate_Test",
            place = Place("Test Place Name", "Test Place Address"),
            description = null,
            siteUrl = "http://test.com",
            favoritesCount = 10,
            commentsCount = 5,
            dateOfPublication = LocalDate.now().toEpochDay() * 24 * 60 * 60
        )

        assertEquals(LocalDate.now(), news.publicationDate)
    }

    @Test
    fun testRatingCalculation() {
        val news = News(
            id = 2,
            title = "RatingCalculation_Test",
            place = null,
            description = "Test News Description",
            siteUrl = "http://test.com",
            favoritesCount = 10,
            commentsCount = 0,
            dateOfPublication = LocalDate.now().toEpochDay() * 24 * 60 * 60
        )

        assertAlmostEquals(1.0, news.rating, delta = 0.0001)
    }

    private fun assertAlmostEquals(expected: Double, actual: Double, delta: Double = 0.0001) {
        assert(abs(expected - actual) < delta) { "Expected: $expected but was: $actual" }
    }
}
