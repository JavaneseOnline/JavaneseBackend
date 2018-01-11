package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun CourseHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        taskDao: TaskDao,
        layout: Layout,
        coursePage: (idx: Page, treePage: Page, Course, Chapters, prevNext: Pair<Course.BasicInfo?, Course.BasicInfo?>) -> Layout.Page
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    val idx = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Tree)!!
    val tree = chapters(course.basicInfo, chapterDao, lessonDao, taskDao)
    val previousAndNext = courseDao.findPreviousAndNextBasic(course)

    call.respondHtml {
        layout(this, coursePage(idx, treePage, course, tree, previousAndNext))
    }
}
