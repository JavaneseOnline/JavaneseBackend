package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.model.Lesson
import online.javanese.model.LessonDao
import online.javanese.model.LessonTree


fun LessonHandler(
        lessonDao: LessonDao,
        lessonTemplate: (Lesson, LessonTree, prev: LessonTree?, next: LessonTree?) -> String
): suspend (LessonTree, ApplicationCall) -> Unit = { tree, call ->

    val lesson = lessonDao.findById(tree.id)!!
    val lessons = tree.chapter.lessons
    val idx = lessons.indexOf(tree)
    val prev = (idx - 1).let { if (it < 0) null else lessons[it] }
    val next = (idx + 1).let { if (it < lessons.size) lessons[it] else null }

    call.respondText(lessonTemplate(lesson, tree, prev, next), ContentType.Text.Html)

}
