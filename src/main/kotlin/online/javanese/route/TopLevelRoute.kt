package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.Page
import online.javanese.model.PageDao
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun createTopLevelRouteHandler(
        pageDao: PageDao, indexPageBinding: (Page) -> String
) : suspend (ApplicationCall, String) -> Unit = { call, query ->
    val page = pageDao.findByUrlPathComponent(query) // todo: handle other cases
    if (page != null) {
        call.respondText(indexPageBinding(page), ContentType.Text.Html)
    } else {
        throw NotFoundException("page with address '$query' was not found")
    }
}
