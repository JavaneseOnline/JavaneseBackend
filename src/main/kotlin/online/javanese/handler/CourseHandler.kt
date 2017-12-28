package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.model.Course
import online.javanese.model.CourseDao
import online.javanese.model.CourseTree


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
