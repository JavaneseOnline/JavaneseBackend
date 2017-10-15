package online.javanese.handler

import online.javanese.model.Article
import online.javanese.model.ArticleDao
import online.javanese.model.Page
import online.javanese.repository.CourseRepository
import online.javanese.repository.CourseTree
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun PageHandler(
        courseRepo: CourseRepository,
        articleDao: ArticleDao,
        indexPageTpl: (Page) -> String,
        treePageTpl: (Page, List<CourseTree>) -> String,
        articlesPageTpl: (Page, List<Article.BasicInfo>) -> String,
        pageTpl: (Page) -> String
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
    call.respondText(
            when (page.magic) {
                Page.Magic.Index -> indexPageTpl(page)
                Page.Magic.Tree -> treePageTpl(page, courseRepo.findTreeSortedBySortIndex())
                Page.Magic.Articles -> articlesPageTpl(page, articleDao.findAllBasicPublishedOrderBySortIndex())
                Page.Magic.CodeReview -> pageTpl(page)
            },
            ContentType.Text.Html
    )
}
