package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.model.Article
import online.javanese.model.Page


fun ArticleHandler(
        articleTemplate: (Page, Article) -> String
): suspend (Page, Article, ApplicationCall) -> Unit = { page, article, call ->
    call.respondText(articleTemplate(page, article), ContentType.Text.Html)
}
