package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondTextWriter
import io.ktor.util.escapeHTML
import online.javanese.link.Link
import online.javanese.locale.Language
import online.javanese.model.*
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*
import kotlin.concurrent.getOrSet


private fun createFormat() = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
private val timeFormat = ThreadLocal<SimpleDateFormat>()

fun ArticleRssHandler(
        pageDao: PageDao,
        articleDao: ArticleDao,
        pageLink: Link<Page, *>,
        articleLink: Link<Article.BasicInfo, *>,
        siteUrl: String,
        feedInfo: Language.FeedInfo
): suspend (ApplicationCall) -> Unit = { call ->

    val articlesUrl = siteUrl + pageLink.url(pageDao.findByMagic(Page.Magic.Articles)!!)

    val items = articleDao
            .findAllBasicPublished()
            .map {
                RssItem(
                        title = it.linkText,
                        description = it.meta.description,
                        link = siteUrl + articleLink.url(it),
                        pubDate = it.createdAt
                )
            }

    call.respondTextWriter(contentType = ContentType.Application.Rss, writer = {
        write("""<?xml version="1.0" encoding="utf-8"?>""")
        write("""<rss version="2.0">""")

        write("<channel>")
        write("<title>"); write(feedInfo.title.escapeHTML()); write("</title>")
        write("<description>"); write(feedInfo.description.escapeHTML()); write("</description>")
        write("<link>"); write(articlesUrl.escapeHTML()); write("</link>")

        items.forEach {
            write("<item>")
            write("<title>"); write(it.title.escapeHTML()); write("</title>")
            write("<description>"); write(it.description.escapeHTML()); write("</description>")
            write("<link>"); write(it.link.escapeHTML()); write("</link>")
            write("<pubDate>")
            write(timeFormat.getOrSet(::createFormat).format(Date.from(it.pubDate.atZone(ZoneId.systemDefault()).toInstant())))
            write("</pubDate>")
            write("</item>")
        }

        write("</channel>")
        write("</rss>")
    })

}
