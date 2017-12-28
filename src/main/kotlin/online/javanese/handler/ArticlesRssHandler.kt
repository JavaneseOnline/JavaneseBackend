package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.model.ArticleDao
import online.javanese.model.RssItem


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
