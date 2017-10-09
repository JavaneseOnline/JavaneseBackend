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
        indexPageBinding: (Page) -> String,
        treePageBinding: (Page, List<CourseTree>) -> String,
        articlesPageBinding: (Page, List<Article.BasicInfo>) -> String,
        pageBinding: (Page) -> String
): suspend (ApplicationCall, Page) -> Unit = { call, page ->
    call.respondText(
            when (page.magic) {
                Page.Magic.Index -> indexPageBinding(page)
                Page.Magic.Tree -> treePageBinding(page, courseRepo.findTreeSortedBySortIndex())
                Page.Magic.Articles -> articlesPageBinding(page, articleRepo.findAllBasicOrderBySortIndex())
                Page.Magic.CodeReview -> pageBinding(page)
            },
            ContentType.Text.Html
    )
}