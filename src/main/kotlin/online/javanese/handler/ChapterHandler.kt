package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun ChapterHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        taskDao: TaskDao,
        layout: Layout,
        page: (idxPage: Page, treePage: Page, Course.BasicInfo, Chapter, Lessons, prevAndNext: Pair<Chapter.BasicInfo?, Chapter.BasicInfo?>) -> Layout.Page
): suspend (ApplicationCall, Chapter.BasicInfo) -> Unit = { call, basicChapter ->

    val idxPage = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Courses)!!
    val chapter = chapterDao.findById(basicChapter.id)!!
    val course = courseDao.findBasicById(basicChapter.courseId)!!

    val lessons = lessons(basicChapter, lessonDao, taskDao)

    val prevAndNext = chapterDao.findPreviousAndNextBasic(chapter)

    call.respondHtml {
        layout(this, page(idxPage, treePage, course, chapter, lessons, prevAndNext))
    }

}
