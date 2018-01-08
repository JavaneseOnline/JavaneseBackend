package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import kotlinx.html.HTML
import online.javanese.model.*


fun CourseHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        coursePage: HTML.(treePage: Page, Course, CourseTree, prev: Course.BasicInfo?, next: Course.BasicInfo?) -> Unit
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    val treePage = pageDao.findByMagic(Page.Magic.Tree)!!
    val tree = courseDao.findTree(course.basicInfo.id)!!
    val previous = courseDao.findPrevious(course)
    val next = courseDao.findNext(course)

    call.respondHtml {
        coursePage(treePage, course, tree, previous, next)
    }
}
