package online.javanese.page

import kotlinx.html.*
import online.javanese.handler.Lessons
import online.javanese.locale.Language
import online.javanese.model.*


class ChapterPage(
        private val indexPage: Page,
        private val treePage: Page,
        private val course: Course.BasicInfo,
        private val chapter: Chapter, // fixme: basic info here
        private val lessons: Lessons,
        private val previousAndNext: Pair<Chapter.BasicInfo?, Chapter.BasicInfo?>,
        private val pageLink: Link<Page>,
        private val courseLink: Link<Course.BasicInfo>,
        private val chapterLink: Link<Chapter.BasicInfo>,
        private val lessonLink: Link<Lesson.BasicInfo>,
        private val taskLink: Link<Task.BasicInfo>,
        private val language: Language
) : Layout.Page {

    override val meta: Meta get() = chapter.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardDiv {
            nav {
                pageLink.insert(this, indexPage)
                +" / "
                pageLink.insert(this, treePage)
                +" / "
                courseLink.insert(this, course)
            }

            main {
                h1(classes = "content-padding-v") {
                    +chapter.h1
                }

                unsafe {
                    +chapter.description
                }
            }

            nav {
                TreeMode.Lessons.lessonsTree(this, lessons, lessonLink, taskLink)
            }

            prevNextPane(
                    previousAndNext, chapterLink, language.previousChapter,
                    language.nextChapter
            )
        }
    }

    override fun scripts(body: BODY) = Unit

}
