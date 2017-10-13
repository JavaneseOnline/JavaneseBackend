package online.javanese.handler

import online.javanese.model.Course
import online.javanese.repository.CourseRepository
import online.javanese.repository.CourseTree
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun CourseHandler(
        courseRepository: CourseRepository,
        coursePageTpl: (Course, CourseTree, prev: Course.BasicInfo?, next: Course.BasicInfo?) -> String
): suspend (ApplicationCall, Course) -> Unit = { call, course ->

    call.respondText(
            coursePageTpl(
                    course,
                    courseRepository.findTree(course.basicInfo.id)!!,
                    courseRepository.findPrevious(course),
                    courseRepository.findNext(course)
            ),
            ContentType.Text.Html
    )

}
