package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import kotlinx.html.HTML
import online.javanese.model.*


fun PageHandler(
        courseDao: CourseDao,
        articleDao: ArticleDao,
        indexPage: HTML.(Page) -> Unit,
        treePage: HTML.(Page, List<CourseTree>) -> Unit,
        articlesPage: HTML.(Page, List<Article.BasicInfo>) -> Unit,
        codeReviewTpl: HTML.(Page) -> Unit
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
    when (page.magic) {
        Page.Magic.Index ->
            call.respondHtml { indexPage(page) }
        Page.Magic.Tree -> {
            val tree = courseDao.findTreeSortedBySortIndex()
            call.respondHtml { treePage(page, tree) }
        }
        Page.Magic.Articles -> {
            val articles = articleDao.findAllBasicPublished()
            call.respondHtml { articlesPage(page, articles) }
        }
        Page.Magic.CodeReview ->
            call.respondHtml { codeReviewTpl(page) }
    }
}
