package online.javanese.route

import io.ktor.application.ApplicationCall
import online.javanese.exception.NotFoundException
import online.javanese.model.ChapterTree
import online.javanese.model.CourseTree
import online.javanese.model.LessonTree


fun ThreePartsRoute(
        tree: List<CourseTree>,
        lessonHandler: suspend (course: CourseTree, chapter: ChapterTree, lesson: LessonTree, call: ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String, String) -> Unit = f@ { call, first, second, third ->

    tree.firstOrNull { it.urlPathComponent == first }?.let { course ->
                course.chapters.firstOrNull { it.urlPathComponent == second }?.let { chapter ->
                            chapter.lessons.firstOrNull { it.urlPathComponent == third }?.let { lesson ->
                                        return@f lessonHandler(course, chapter, lesson, call)
                                    }
                        }
            }

    throw NotFoundException("can't find lesson for address /$first/$second/$third/")
}
