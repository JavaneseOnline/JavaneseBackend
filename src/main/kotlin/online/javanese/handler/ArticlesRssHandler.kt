package online.javanese.handler

import online.javanese.model.ArticleDao
import online.javanese.model.RssItem
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun ArticleRssHandler(
        siteUrl: String,
        articleDao: ArticleDao,
        rssFeedTemplate: (List<RssItem>) -> String
): suspend (ApplicationCall) -> Unit = { call ->

    val items = articleDao
            .findAllPublishedOrderBySortIndex()
            .map {
                RssItem(
                        title = it.basicInfo.linkText,
                        description = it.meta.description,
                        link = siteUrl + it.basicInfo.urlPathComponent,
                        pubDate = it.createdAt
                )
            }

    call.respondText(rssFeedTemplate(items), ContentType.Application.Rss)

}
