package online.javanese.handler

import online.javanese.model.Course
import online.javanese.model.CourseDao
import online.javanese.model.CourseTree
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun CourseHandler(
        courseDao: CourseDao,
        coursePageTpl: (Course, CourseTree, prev: Course.BasicInfo?, next: Course.BasicInfo?) -> String
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    call.respondText(
            coursePageTpl(
                    course,
                    courseDao.findTree(course.basicInfo.id)!!,
                    courseDao.findPrevious(course),
                    courseDao.findNext(course)
            ),
            ContentType.Text.Html
    )

}
