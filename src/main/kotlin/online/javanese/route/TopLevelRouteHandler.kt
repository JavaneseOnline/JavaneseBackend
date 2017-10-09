package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.Page
import online.javanese.repository.PageRepository
import org.jetbrains.ktor.application.ApplicationCall

fun TopLevelRouteHandler(
        pageRepo: PageRepository,
        pageHandler: suspend (ApplicationCall, Page) -> Unit
) : suspend (ApplicationCall, String) -> Unit = { call, query ->
    val page = pageRepo.findByUrlPathComponent(query)
    if (page != null) {
        pageHandler(call, page)
    } else {
        throw NotFoundException("page with address '$query' was not found") // todo
    }
}
