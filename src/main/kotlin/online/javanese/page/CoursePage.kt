package online.javanese.page

import kotlinx.html.*
import online.javanese.handler.Chapters
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.locale.Language
import online.javanese.model.*


class CoursePage(
        private val course: Course,
        private val chapters: Chapters,
        private val previousAndNext: Pair<Course.BasicInfo?, Course.BasicInfo?>,
        private val courseLink: Link<Course.BasicInfo>,
        private val chapterLink: Link<Chapter.BasicInfo>,
        private val lessonLink: Link<Lesson.BasicInfo>,
        private val taskLink: Link<Task.BasicInfo>,
        private val language: Language,
        private val beforeContent: HtmlBlock
) : Layout.Page {

    override val meta: Meta get() = course.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            main {
                h1(classes = "content-padding-v") {
                    +course.heading
                }

                unsafe {
                    +course.description
                }
            }

            nav {
                chaptersTree(chapters, chapterLink, lessonLink, taskLink, TreeMode.Lessons)
            }

            prevNextPane(previousAndNext, courseLink, language.previousCourse, language.nextCourse)
        }
    }

    override fun scripts(body: BODY) = Unit

}
