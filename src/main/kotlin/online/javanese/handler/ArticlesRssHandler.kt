package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.extensions.encodeForUrl
import online.javanese.model.ArticleDao
import online.javanese.model.RssItem


fun ArticleRssHandler(
        siteUrl: String,
        articleDao: ArticleDao,
        rssFeedTemplate: (List<RssItem>) -> String
): suspend (ApplicationCall) -> Unit = { call ->

    val статьи = "статьи".encodeForUrl()

    val items = articleDao
            .findAllPublished()
            .map {
                RssItem(
                        title = it.basicInfo.linkText,
                        description = it.meta.description,
                        link = siteUrl + '/' + статьи + '/' + it.basicInfo.urlPathComponent.encodeForUrl(),
                        pubDate = it.createdAt
                )
            }

    call.respondText(rssFeedTemplate(items), ContentType.Application.Rss)

}
