@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.template

import kotlinx.html.*
import online.javanese.model.ChapterTree
import online.javanese.model.CourseTree
import online.javanese.model.LessonTree
import online.javanese.model.TaskTree

fun FlowContent.coursesTree(
        courses: List<CourseTree>,
        urlOfCourse: (CourseTree) -> String, urlOfChapter: (ChapterTree) -> String,
        urlOfLesson: (LessonTree) -> String, urlOfTask: (TaskTree) -> String,
        mode: TreeMode
) = ul {
    courses.forEach { course ->
        li {
            linkTo(urlOfCourse, course)

            chaptersTree(course.chapters, urlOfChapter, urlOfLesson, urlOfTask, mode)
        }
    }
}

fun FlowContent.chaptersTree(
        chapters: List<ChapterTree>,
        urlOfChapter: (ChapterTree) -> String, urlOfLesson: (LessonTree) -> String, urlOfTask: (TaskTree) -> String,
        mode: TreeMode
) = ul {
    chapters.forEach { chapter ->
        li {
            linkTo(urlOfChapter, chapter)

            mode.lessonsTree(this, chapter.lessons, urlOfLesson, urlOfTask)
        }
    }
}

enum class TreeMode {
    Lessons {
        override fun lessonsTree(
                doc: FlowContent, lessons: List<LessonTree>,
                urlOfLesson: (LessonTree) -> String, urlOfTask: (TaskTree) -> String
        ) = with (doc) {
            ul {
                lessons.forEach { lesson ->
                    li {
                        linkTo(urlOfLesson, lesson)
                    }
                }
            }
        }
    }, Tasks {
        override fun lessonsTree(
                doc: FlowContent, lessons: List<LessonTree>,
                urlOfLesson: (LessonTree) -> String, urlOfTask: (TaskTree) -> String
        ) = with(doc) {
            ul {
                lessons.forEach { lesson ->
                    li {
                        small { +lesson.linkText }

                        tasksTree(lesson.tasks, urlOfTask)
                    }
                }
            }
        }
    };

    abstract fun lessonsTree(
            doc: FlowContent, lessons: List<LessonTree>,
            urlOfLesson: (LessonTree) -> String, urlOfTask: (TaskTree) -> String
    )
}

fun FlowContent.tasksTree(tasks: List<TaskTree>, urlOfTask: (TaskTree) -> String) = ul {
    tasks.forEach { task ->
        li {
            linkTo(urlOfTask, task)
        }
    }
}

fun <T : Any> FlowContent.prevNextPane(previous: T?, next: T?, urlOf: (T) -> String, linkTextOf: (T) -> String, prevText: String, nextText: String) {
    if (previous != null || next != null) {
        nav(classes = "mdl-grid mdl-grid--no-spacing") {
            p(classes = "mdl-cell mdl-cell--4-col mdl-cell--6-col-desktop") {
                previous?.let {
                    a(href = urlOf(it), titleAndText = prevText)
                }
            }

            p(classes = "mdl-cell mdl-cell--4-col-tablet mdl-cell--6-col-desktop mdl-cell--hide-phone mdl-typography--text-right") {
                next?.let {
                    a(href = urlOf(it), titleAndText = nextText)
                }
            }
            p(classes = "mdl-cell mdl-cell--4-col mdl-cell--hide-desktop mdl-cell--hide-tablet") {
                next?.let {
                    a(href = urlOf(it), titleAndText = nextText)
                }
            }
        }
    }
}

private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(urlOfCourse: (CourseTree) -> String, course: CourseTree) =
        a(href = urlOfCourse(course), titleAndText = course.linkText)

private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(urlOfChapter: (ChapterTree) -> String, chapter: ChapterTree) =
        a(href = urlOfChapter(chapter), titleAndText = chapter.linkText)

private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(urlOfLesson: (LessonTree) -> String, lesson: LessonTree) =
        a(href = urlOfLesson(lesson), titleAndText = lesson.linkText)

private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(urlOfTask: (TaskTree) -> String, task: TaskTree) =
        a(href = urlOfTask(task), titleAndText = task.linkText)
