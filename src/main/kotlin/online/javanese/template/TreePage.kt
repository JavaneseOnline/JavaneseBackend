@file:Suppress("NOTHING_TO_INLINE")
package online.javanese.template

import kotlinx.html.*
import online.javanese.model.*
import java.util.*

class TreePage(
        private val page: Page,
        private val courses: List<CourseTree>,
        private val messages: Properties,
        private val urlOfCourse: (CourseTree) -> String,
        private val urlOfChapter: (ChapterTree) -> String,
        private val urlOfLesson: (LessonTree) -> String,
        private val urlOfTask: (TaskTree) -> String
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                a(href = "/") {
                    +messages.getProperty("index.title")
                }
            }

            h1 {
                +page.h1
            }

            unsafe {
                +page.bodyMarkup
            }

            tabs {
                tabBar {
                    tabLink("lessons", messages.getProperty("tree.tab.lessons"), true)
                    tabLink("tasks", messages.getProperty("tree.tab.tasks"))
                }

                tabPanel("lessons", true) {
                    coursesTree()
                }

                tabPanel("tasks") {
                    coursesTasksTree()
                }
            }
        }
    }

    private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(course: CourseTree) =
            a(href = urlOfCourse(course), titleAndText = course.linkText)

    private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(chapter: ChapterTree) =
            a(href = urlOfChapter(chapter), titleAndText = chapter.linkText)

    private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(lesson: LessonTree) =
            a(href = urlOfLesson(lesson), titleAndText = lesson.linkText)

    private inline fun FlowOrInteractiveOrPhrasingContent.linkTo(task: TaskTree) =
            a(href = urlOfTask(task), titleAndText = task.linkText)

    private fun FlowContent.coursesTree() = ul {
        courses.forEach { course ->
            li {
                linkTo(course)

                ul {
                    course.chapters.forEach { chapter ->
                        li {
                            linkTo(chapter)

                            ul {
                                chapter.lessons.forEach { lesson ->
                                    li {
                                        linkTo(lesson)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun FlowContent.coursesTasksTree() = ul {
        courses.forEach { course ->
            li {
                linkTo(course)

                course.chapters.forEach { chapter ->
                    linkTo(chapter)

                    ul {
                        chapter.lessons.forEach { lesson ->
                            li {
                                small { +lesson.linkText }

                                ul {
                                    lesson.tasks.forEach { task ->
                                        li {
                                            linkTo(task)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
