package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.*

class TreePage(
        private val indexPage: Page,
        private val page: Page,
        private val courses: List<CourseTree>,
        private val language: Language,
        private val urlOfCourse: (CourseTree) -> String,
        private val urlOfChapter: (ChapterTree) -> String,
        private val urlOfLesson: (LessonTree) -> String,
        private val urlOfTask: (TaskTree) -> String,
        private val linkToPage: Link<Page>
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                a(href = "/") {
                    linkToPage.insert(this, indexPage)
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
                    tabLink("lessons", language.lessonsTreeTab, true)
                    tabLink("tasks", language.tasksTreeTab)
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
