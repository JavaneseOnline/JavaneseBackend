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
): suspend (ApplicationCall, Course.BasicInfo) -> Unit = { call, basicCourse ->

    val idx = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Courses)!!
    val tree = chapters(basicCourse, chapterDao, lessonDao, taskDao)

    val course = courseDao.findById(basicCourse.id)!!
    val previousAndNext = courseDao.findPreviousAndNextBasic(course)

    call.respondHtml {
        layout(this, coursePage(idx, treePage, course, tree, previousAndNext))
    }
}
