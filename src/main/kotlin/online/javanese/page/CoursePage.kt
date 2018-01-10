package online.javanese.page

import kotlinx.html.*
import online.javanese.locale.Language
import online.javanese.model.*

class CoursePage(
        private val indexPage: Page,
        private val treePage: Page,
        private val course: Course,
        private val courseTree: CourseTree,
        private val previous: Course.BasicInfo?,
        private val next: Course.BasicInfo?,
        private val pageLink: Link<Page>,
        private val urlOfCourse: (Course.BasicInfo) -> String,
        private val urlOfChapter: (ChapterTree) -> String,
        private val urlOfLesson: (LessonTree) -> String,
        private val urlOfTask: (TaskTree) -> String,
        private val language: Language
) : Layout.Page {

    override val meta: Meta get() = course.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                pageLink.insert(this, indexPage)
                +" / "
                pageLink.insert(this, treePage)
            }

            main {
                h1(classes = "content-padding-v") {
                    +course.h1
                }

                unsafe {
                    +course.description
                }
            }

            nav {
                chaptersTree(courseTree.chapters, urlOfChapter, urlOfLesson, urlOfTask, TreeMode.Lessons)
            }

            prevNextPane(
                    previous, next, urlOfCourse, language.previousCourse,
                    language.nextCourse
            )
        }
    }

    override fun scripts(body: BODY) = Unit

}
