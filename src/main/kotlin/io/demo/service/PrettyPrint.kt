package io.demo.service

import io.demo.dto.News
import mu.KotlinLogging
import java.io.File

@DslMarker
annotation class PrettyPrintMarker

private val logger = KotlinLogging.logger {}

@PrettyPrintMarker
class NewsBuilder {
    private val content = StringBuilder()

    fun header(level: Int, init: HeaderBuilder.() -> Unit) {
        val headerBuilder = HeaderBuilder(level)
        headerBuilder.init()
        content.append(headerBuilder.build())
    }

    fun text(init: TextBuilder.() -> Unit) {
        val textBuilder = TextBuilder()
        textBuilder.init()
        content.append(textBuilder.build())
    }

    fun build(): String {
        return content.toString()
    }
}

class HeaderBuilder(private val level: Int) {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun build(): String {
        return "".repeat(level) + " " + content.toString() + "\n"
    }
}

class TextBuilder {
    private val content = StringBuilder()

    operator fun String.unaryPlus() {
        content.append(this)
    }

    fun bold(text: String): String {
        return "**$text**"
    }

    fun build(): String {
        return content.toString() + "\n"
    }
}

fun buildPrettyNews(news: List<News>): String {
    val builder = NewsBuilder()
    builder.header(level = 1) { ("News reports").unaryPlus() }
    builder.header(level = 1) { ("------------------------------\n").unaryPlus() }

    news.forEach { article ->
        builder.text {
            builder.text { (bold(" ${article.title} ")).unaryPlus() }
            builder.text { ("\n Place: ${article.place?.title ?: "Unknown"}").unaryPlus() }
            builder.text { (" Description: ${article.description ?: "No description"}").unaryPlus() }
            builder.text { (" Read more here: ${article.siteUrl ?: "No URL"}").unaryPlus() }
            builder.text { (" Favorites Count: ${article.favoritesCount}").unaryPlus() }
            builder.text { (" Comments Count: ${article.commentsCount}").unaryPlus() }
            builder.text { (" Publication Date: ${article.publicationDate}").unaryPlus() }
            builder.text { (" Rating: ${article.rating ?: "Unknown"}\n").unaryPlus() }
            builder.text { (" ------------------------------").unaryPlus() }
        }
    }

    val prettyPrintContent = builder.build()
    println(prettyPrintContent)
    return prettyPrintContent
}

fun savePrettyNews(news: List<News>, basePath: String) {
    val content = buildPrettyNews(news)

    var filePath = basePath
    var count = 1

    while (File(filePath).exists()) {
        filePath = "${basePath}_$count"
        count++
    }

    try {
        File(filePath).bufferedWriter().use { writer ->
            writer.write(content)
        }

        logger.info("Pretty printed news saved to file $filePath")

    } catch (e: Exception) {
        logger.error("Failed to save pretty printed news to file $filePath", e)
    }
}
