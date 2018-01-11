package online.javanese.route

import io.ktor.application.ApplicationCall
import online.javanese.exception.NotFoundException
import online.javanese.model.*


fun ThreePartsRoute(
        courseDao: CourseDao, chapterDao: ChapterDao, lessonDao: LessonDao,
        lessonHandler: suspend (course: Course.BasicInfo, chapter: Chapter.BasicInfo, lesson: Lesson, call: ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String, String) -> Unit = f@ { call, first, second, third ->

    courseDao.findBasicByUrlSegment(first)?.let { course ->
        chapterDao.findBasicByUrlSegment(course.id, second)?.let { chapter ->
            lessonDao.findByUrlSegment(chapter.id, third)?.let { lesson ->
                return@f lessonHandler(course, chapter, lesson, call)
            }
        }
    }

    throw NotFoundException("can't find lesson for address /$first/$second/$third/")
}
