package online.javanese.page

import kotlinx.html.*
import online.javanese.handler.Chapters
import online.javanese.handler.Courses
import online.javanese.model.Chapter
import online.javanese.model.Course
import online.javanese.model.Lesson
import online.javanese.model.Task
import online.javanese.handler.Lessons as MLessons


fun FlowContent.coursesTree(
        courses: Courses,
        courseLink: Link<Course.BasicInfo>,
        chapterLink: Link<Chapter.BasicInfo>,
        lessonLink: Link<Lesson.BasicInfo>,
        taskLink: Link<Task.BasicInfo>,
        mode: TreeMode
) = ul {
    courses.forEach { (course, chapters) ->
        li {
            courseLink.render(this, course)

            chaptersTree(chapters, chapterLink, lessonLink, taskLink, mode)
        }
    }
}

fun FlowContent.chaptersTree(
        chapters: Chapters,
        chapterLink: Link<Chapter.BasicInfo>,
        lessonLink: Link<Lesson.BasicInfo>,
        taskLink: Link<Task.BasicInfo>,
        mode: TreeMode
) = ul {
    chapters.forEach { (chapter, lessons) ->
        li {
            chapterLink.render(this, chapter)

            mode.lessonsTree(this, lessons, lessonLink, taskLink)
        }
    }
}

enum class TreeMode {
    Lessons {
        override fun lessonsTree(
                doc: FlowContent, lessons: MLessons,
                lessonLink: Link<Lesson.BasicInfo>, taskLink: Link<Task.BasicInfo>
        ) = with (doc) {
            ul {
                lessons.forEach { (lesson, _) ->
                    li {
                        lessonLink.render(this, lesson)
                    }
                }
            }
        }
    }, Tasks {
        override fun lessonsTree(
                doc: FlowContent, lessons: MLessons,
                lessonLink: Link<Lesson.BasicInfo>, taskLink: Link<Task.BasicInfo>
        ) = with(doc) {
            ul {
                lessons.forEach { (lesson, tasks) ->
                    li {
                        small { +lessonLink.linkText(lesson) }

                        tasksTree(tasks, taskLink)
                    }
                }
            }
        }
    };

    abstract fun lessonsTree(
            doc: FlowContent, lessons: MLessons,
            lessonLink: Link<Lesson.BasicInfo>, taskLink: Link<Task.BasicInfo>
    )
}

fun FlowContent.tasksTree(tasks: List<Task.BasicInfo>, linkToTask: Link<Task.BasicInfo>) = ul {
    tasks.forEach { task ->
        li {
            linkToTask.render(this, task)
        }
    }
}

fun <T : Any> FlowContent.prevNextPane(
        previousAndNext: Pair<T?, T?>, tLink: Link<T>, prevText: String, nextText: String, moreClasses: String? = null
) {
    val (previous, next) = previousAndNext
    if (previous != null || next != null) {
        nav(classes = classes("mdl-grid mdl-grid--no-spacing", moreClasses)) {
            p(classes = "mdl-cell mdl-cell--4-col mdl-cell--6-col-desktop") {
                previous?.let {
                    tLink.renderCustom(this, it) { +prevText }
                }
            }

            p(classes = "mdl-cell mdl-cell--4-col-tablet mdl-cell--6-col-desktop mdl-cell--hide-phone mdl-typography--text-right") {
                next?.let {
                    tLink.renderCustom(this, it) { +nextText }
                }
            }
            p(classes = "mdl-cell mdl-cell--4-col mdl-cell--hide-desktop mdl-cell--hide-tablet") {
                next?.let {
                    tLink.renderCustom(this, it) { +nextText }
                }
            }
        }
    }
}
