package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondWrite
import io.ktor.util.escapeHTML
import online.javanese.extensions.encodeForUrl
import online.javanese.model.ArticleDao
import online.javanese.model.RssItem
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

private val статьи = "статьи".encodeForUrl() // fixme
private val timeFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)

fun ArticleRssHandler(
        articleDao: ArticleDao
): suspend (ApplicationCall) -> Unit = { call ->

    val items = articleDao
            .findAllPublished()
            .map {
                RssItem(
                        title = it.basicInfo.linkText,
                        description = it.meta.description,
                        link = "http://javanese.online/" + статьи + '/' + it.basicInfo.urlPathComponent.encodeForUrl() + '/',
                        pubDate = it.createdAt
                )
            }

    call.respondWrite(contentType = ContentType.Application.Rss) {
        write("""<?xml version="1.0" encoding="utf-8"?>""")
        write("""<rss version="2.0">""")

        write("<channel>")
        write("<title>Статьи на Javanese.Online</title>") // todo: resources
        write("<description>Статьи о Java, Kotlin и Android</description>")
        write("<link>http://javanese.online/%D1%81%D1%82%D0%B0%D1%82%D1%8C%D0%B8/</link>") // todo: real page

        items.forEach {
            write("<item>")
            write("<title>"); write(it.title.escapeHTML()); write("</title>")
            write("<description>"); write(it.description.escapeHTML()); write("</description>")
            write("<link>"); write(it.link.escapeHTML()); write("</link>")
            write("<pubDate>");
            write(timeFormat.format(Date.from(it.pubDate.atZone(ZoneId.systemDefault()).toInstant())));
            write("</pubDate>")
            write("</item>")
        }

        write("</channel>")
        write("</rss>")
    }

}
