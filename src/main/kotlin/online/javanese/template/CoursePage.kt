package online.javanese.template

import kotlinx.html.*
import online.javanese.model.*
import java.util.*

class CoursePage(
        private val course: Course,
        private val courseTree: CourseTree,
        private val previous: Course.BasicInfo?,
        private val next: Course.BasicInfo?,
        private val urlOfCourse: (Course.BasicInfo) -> String,
        private val urlOfChapter: (ChapterTree) -> String,
        private val urlOfLesson: (LessonTree) -> String,
        private val urlOfTask: (TaskTree) -> String,
        private val messages: Properties
) : Layout.Page {

    override val meta: Meta get() = course.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                a(href = "/", titleAndText = messages.getProperty("index.title"))
                +" / "
                a(href = urlOfCourse(course.basicInfo), titleAndText = courseTree.linkText)
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
                    previous, next, urlOfCourse, Course.BasicInfo::linkText,
                    messages.getProperty("course.previous"), messages.getProperty("course.next")
            )
        }
    }

    override fun scripts(body: BODY) = Unit

}
