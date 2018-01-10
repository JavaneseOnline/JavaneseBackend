package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun CourseHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        layout: Layout,
        coursePage: (idx: Page, treePage: Page, Course, CourseTree, prev: Course.BasicInfo?, next: Course.BasicInfo?) -> Layout.Page
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    val idx = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Tree)!!
    val tree = courseDao.findTree(course.basicInfo.id)!!
    val previous = courseDao.findPrevious(course)
    val next = courseDao.findNext(course)

    call.respondHtml {
        layout(this, coursePage(idx, treePage, course, tree, previous, next))
    }
}
