package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun LessonHandler(
        courseDao: CourseDao, chapterDao: ChapterDao, lessonDao: LessonDao, taskDao: TaskDao,
        pageDao: PageDao,
        layout: Layout,
        lessonPage: (index: Page, tree: Page, Course.BasicInfo, Chapter.BasicInfo, Lesson, List<Task>, prevNext: Pair<Lesson.BasicInfo?, Lesson.BasicInfo?>) -> Layout.Page
): suspend (Course.BasicInfo, Chapter.BasicInfo, Lesson, ApplicationCall) -> Unit = { courseTr, chapterTr, lesson, call ->

    val course = courseDao.findBasicById(courseTr.id)!!
    val chapter = chapterDao.findBasicById(chapterTr.id)!!

    val index = pageDao.findByMagic(Page.Magic.Index)!!
    val treePg = pageDao.findByMagic(Page.Magic.Courses)!!

    val tasks = taskDao.findForLessonSorted(lesson.basicInfo.id)
    val prevNext = lessonDao.findPreviousAndNext(lesson)

    call.respondHtml {
        layout(this, lessonPage(index, treePg, course, chapter, lesson, tasks, prevNext))
    }

}
