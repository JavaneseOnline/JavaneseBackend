package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.template.Layout


fun LessonHandler(
        courseDao: CourseDao, chapterDao: ChapterDao, lessonDao: LessonDao,
        pageDao: PageDao,
        layout: Layout,
        lessonPage: (index: Page, tree: Page, Course.BasicInfo, Chapter.BasicInfo, Lesson, LessonTree, prev: LessonTree?, next: LessonTree?) -> Layout.Page
): suspend (CourseTree, ChapterTree, LessonTree, ApplicationCall) -> Unit = { courseTr, chapterTr, lessonTr, call ->

    val course = courseDao.findBasicById(courseTr.id)!!
    val chapter = chapterDao.findBasicById(chapterTr.id)!!
    val lesson = lessonDao.findById(lessonTr.id)!!

    val index = pageDao.findByMagic(Page.Magic.Index)!!
    val treePg = pageDao.findByMagic(Page.Magic.Tree)!!
    val lessons = lessonTr.chapter.lessons
    val idx = lessons.indexOf(lessonTr)
    val prev = (idx - 1).let { if (it < 0) null else lessons[it] }
    val next = (idx + 1).let { if (it < lessons.size) lessons[it] else null }

    call.respondHtml {
        layout(this, lessonPage(index, treePg, course, chapter, lesson, lessonTr, prev, next))
    }

}
