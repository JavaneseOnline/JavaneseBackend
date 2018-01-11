package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun ChapterHandler(
        pageDao: PageDao,
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        taskDao: TaskDao,
        layout: Layout,
        page: (idxPage: Page, treePage: Page, Course.BasicInfo, Chapter, Lessons, prevAndNext: Pair<Chapter.BasicInfo?, Chapter.BasicInfo?>) -> Layout.Page
): suspend (Course.BasicInfo, Chapter, ApplicationCall) -> Unit = { course, chapter, call ->

    val idxPage = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Tree)!!

    val lessons = lessons(chapter.basicInfo, lessonDao, taskDao)

    val prevAndNext = chapterDao.findPreviousAndNextBasic(chapter)

    call.respondHtml {
        layout(this, page(idxPage, treePage, course, chapter, lessons, prevAndNext))
    }

}
