package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import kotlinx.html.HTML
import online.javanese.model.Course
import online.javanese.model.CourseDao
import online.javanese.model.CourseTree


fun CourseHandler(
        courseDao: CourseDao,
        coursePage: HTML.(Course, CourseTree, prev: Course.BasicInfo?, next: Course.BasicInfo?) -> Unit
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    val tree = courseDao.findTree(course.basicInfo.id)!!
    val previous = courseDao.findPrevious(course)
    val next = courseDao.findNext(course)

    call.respondHtml {
        coursePage(course, tree, previous, next)
    }
}
