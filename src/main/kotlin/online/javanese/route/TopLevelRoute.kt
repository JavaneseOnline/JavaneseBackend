package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.Course
import online.javanese.model.CourseDao
import online.javanese.model.Page
import online.javanese.model.PageDao
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun createTopLevelRouteHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        indexPageBinding: (Page) -> String,
        treePageBinding: (Page, List<Course>) -> String
) : suspend (ApplicationCall, String) -> Unit = { call, query ->
    val page = pageDao.findByUrlPathComponent(query)
    if (page != null) {
        when (page.magic) {
            Page.Magic.Index -> call.respondText(indexPageBinding(page), ContentType.Text.Html)
            Page.Magic.Tree -> call.respondText(treePageBinding(page, courseDao.findAllSortedBySortIndex()), ContentType.Text.Html)
            Page.Magic.Articles -> TODO()
            Page.Magic.CodeReview -> TODO()
        }.also { /* exhaustive */ }
    } else {
        throw NotFoundException("page with address '$query' was not found") // todo
    }
}
