package online.javanese.route

import io.ktor.application.ApplicationCall
import online.javanese.exception.NotFoundException
import online.javanese.model.CourseTree
import online.javanese.model.LessonTree


fun ThreePartsRoute(
        tree: List<CourseTree>,
        lessonHandler: suspend (lesson: LessonTree, call: ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String, String) -> Unit = f@ { call, first, second, third ->

    tree
            .firstOrNull { it.urlPathComponent == first }
            ?.chapters
            ?.firstOrNull { it.urlPathComponent == second }
            ?.lessons
            ?.firstOrNull { it.urlPathComponent == third }
            ?.let {
                return@f lessonHandler(it, call)
            }

    throw NotFoundException("neither article nor course exist for address /$first/$second/")
}