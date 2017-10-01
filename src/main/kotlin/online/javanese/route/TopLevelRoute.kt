package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.Page
import online.javanese.repository.CourseRepository
import online.javanese.repository.CourseTree
import online.javanese.repository.PageRepository
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun createTopLevelRouteHandler(
        pageRepo: PageRepository,
        courseRepo: CourseRepository,
        indexPageBinding: (Page) -> String,
        treePageBinding: (Page, List<CourseTree>) -> String
) : suspend (ApplicationCall, String) -> Unit = { call, query ->
    val page = pageRepo.findByUrlPathComponent(query)
    if (page != null) {
        when (page.magic) {
            Page.Magic.Index -> call.respondText(indexPageBinding(page), ContentType.Text.Html)
            Page.Magic.Tree -> call.respondText(
                    treePageBinding(page, courseRepo.findTreeSortedBySortIndex()),
                    ContentType.Text.Html)
            Page.Magic.Articles -> TODO()
            Page.Magic.CodeReview -> TODO()
        }.also { /* exhaustive */ }
    } else {
        throw NotFoundException("page with address '$query' was not found") // todo
    }
}
