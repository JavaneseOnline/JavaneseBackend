package online.javanese.route

import online.javanese.model.Article
import online.javanese.model.Page
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun ArticleHandler(
        articleTemplate: (Page, Article) -> String
): suspend (Page, Article, ApplicationCall) -> Unit = { page, article, call ->
    call.respondText(articleTemplate(page, article), ContentType.Text.Html)
}
