package online.javanese.page

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

            h1(classes = "content-padding-v") {
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

                tabPanelNav("lessons", true) {
                    coursesTree(courses, urlOfCourse, urlOfChapter, urlOfLesson, urlOfTask, TreeMode.Lessons)
                }

                tabPanelNav("tasks") {
                    coursesTree(courses, urlOfCourse, urlOfChapter, urlOfLesson, urlOfTask, TreeMode.Tasks)
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
