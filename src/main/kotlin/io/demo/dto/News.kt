package io.demo.dto

import io.demo.service.PlaceSerializer
import kotlinx.serialization.*
import java.time.LocalDate
import kotlin.math.exp


@Serializable
data class News(
    val id: Int,
    val title: String,
    @Serializable(with = PlaceSerializer::class)
    val place: Place?,
    val description: String?,
    @SerialName("site_url")
    val siteUrl: String,
    @SerialName("favorites_count")
    val favoritesCount: Int,
    @SerialName("comments_count")
    val commentsCount: Int,
    @SerialName("publication_date")
    val dateOfPublication: Long
) {
    val publicationDate: LocalDate
        get() = LocalDate.ofEpochDay(dateOfPublication / (24 * 60 * 60))

    val rating: Double by lazy {
        1 / (1 + exp(-(favoritesCount.toDouble() / (commentsCount + 1).toDouble())))
    }
}


@Serializable
data class Place(
    val title: String,
    val address: String
)


fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    return this.asSequence()
        .filter { it.publicationDate in period }
        .onEach { println("filtered $it") }
        .sortedByDescending { it.rating }
        .take(count)
        .onEach { println("sorted $it") }
        .toList()

}
