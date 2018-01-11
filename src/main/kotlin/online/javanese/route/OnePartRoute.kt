package online.javanese.route

import io.ktor.application.ApplicationCall
import online.javanese.exception.NotFoundException
import online.javanese.model.Course
import online.javanese.model.CourseDao
import online.javanese.model.Page
import online.javanese.model.PageDao


fun OnePartRoute(
        pageDao: PageDao,
        courseDao: CourseDao,
        pageHandler: suspend (ApplicationCall, Page) -> Unit,
        courseHandler: suspend (ApplicationCall, Course) -> Unit
) : suspend (ApplicationCall, String) -> Unit = func@ { call, query ->

    pageDao.findByUrlSegment(query)?.let {
        return@func pageHandler(call, it)
    }

    courseDao.findByUrlSegment(query)?.let {
        return@func courseHandler(call, it)
    }

    throw NotFoundException("Neither page, nor course exist for query $query")
}
