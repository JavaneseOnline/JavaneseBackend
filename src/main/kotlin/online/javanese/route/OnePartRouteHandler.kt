package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.Course
import online.javanese.model.Page
import online.javanese.repository.CourseRepository
import online.javanese.repository.PageRepository
import org.jetbrains.ktor.application.ApplicationCall

fun OnePartRouteHandler(
        pageRepo: PageRepository,
        courseRepo: CourseRepository,
        pageHandler: suspend (ApplicationCall, Page) -> Unit,
        courseHandler: suspend (ApplicationCall, Course) -> Unit
) : suspend (ApplicationCall, String) -> Unit = func@ { call, query ->

    pageRepo.findByUrlPathComponent(query)?.let {
        return@func pageHandler(call, it)
    }

    courseRepo.findByUrlComponent(query)?.let {
        return@func courseHandler(call, it)
    }

    throw NotFoundException("Neither page, nor course exist for query $query")
}
