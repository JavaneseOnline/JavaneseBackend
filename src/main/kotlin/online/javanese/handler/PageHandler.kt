package online.javanese.handler

import online.javanese.model.*
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun PageHandler(
        courseDao: CourseDao,
        articleDao: ArticleDao,
        indexPageTpl: (Page) -> String,
        treePageTpl: (Page, List<CourseTree>) -> String,
        articlesPageTpl: (Page, List<Article.BasicInfo>) -> String,
        pageTpl: (Page) -> String
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
    call.respondText(
            when (page.magic) {
                Page.Magic.Index -> indexPageTpl(page)
                Page.Magic.Tree -> treePageTpl(page, courseDao.findTreeSortedBySortIndex())
                Page.Magic.Articles -> articlesPageTpl(page, articleDao.findAllBasicPublishedOrderBySortIndex())
                Page.Magic.CodeReview -> pageTpl(page)
            },
            ContentType.Text.Html
    )
}
