package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.repository.CourseTree
import online.javanese.repository.LessonTree
import org.jetbrains.ktor.application.ApplicationCall

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