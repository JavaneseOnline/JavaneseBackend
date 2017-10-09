package online.javanese.route

import online.javanese.model.Article
import online.javanese.model.Page
import online.javanese.repository.ArticleRepository
import online.javanese.repository.CourseRepository
import online.javanese.repository.CourseTree
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun PageHandler(
        courseRepo: CourseRepository,
        articleRepo: ArticleRepository,
        indexPageTpl: (Page) -> String,
        treePageTpl: (Page, List<CourseTree>) -> String,
        articlesPageTpl: (Page, List<Article.BasicInfo>) -> String,
        pageTpl: (Page) -> String
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
    call.respondText(
            when (page.magic) {
                Page.Magic.Index -> indexPageTpl(page)
                Page.Magic.Tree -> treePageTpl(page, courseRepo.findTreeSortedBySortIndex())
                Page.Magic.Articles -> articlesPageTpl(page, articleRepo.findAllBasicOrderBySortIndex())
                Page.Magic.CodeReview -> pageTpl(page)
            },
            ContentType.Text.Html
    )
}