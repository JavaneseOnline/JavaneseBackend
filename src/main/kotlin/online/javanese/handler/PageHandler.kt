package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.response.respondText
import kotlinx.html.HTML
import online.javanese.model.*


fun PageHandler(
        courseDao: CourseDao,
        articleDao: ArticleDao,
        indexPageTpl: HTML.(Page) -> Unit,
        treePageTpl: (Page, List<CourseTree>) -> String,
        articlesPageTpl: (Page, List<Article.BasicInfo>) -> String,
        codeReviewTpl: HTML.(Page) -> Unit
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
        when (page.magic) {
            Page.Magic.Index -> call.respondHtml { indexPageTpl(page) }
            Page.Magic.Tree -> call.respondText(treePageTpl(page, courseDao.findTreeSortedBySortIndex()), ContentType.Text.Html)
            Page.Magic.Articles -> call.respondText(articlesPageTpl(page, articleDao.findAllBasicPublished()), ContentType.Text.Html)
            Page.Magic.CodeReview -> call.respondHtml { codeReviewTpl(page) }
        }
}
