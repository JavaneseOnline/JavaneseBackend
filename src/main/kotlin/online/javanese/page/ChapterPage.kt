package online.javanese.page

import kotlinx.html.*
import online.javanese.handler.Lessons
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.locale.Language
import online.javanese.model.*


class ChapterPage(
        private val chapter: Chapter,
        private val lessons: Lessons,
        private val previousAndNext: Pair<Chapter.BasicInfo?, Chapter.BasicInfo?>,
        private val chapterLink: Link<Chapter.BasicInfo, *>,
        private val lessonLink: Link<Lesson.BasicInfo, *>,
        private val taskLink: Link<Task.BasicInfo, *>,
        private val language: Language,
        private val beforeContent: HtmlBlock
) : Layout.Page {

    override val meta: Meta get() = chapter.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this)

            main {
                h1(classes = "content-padding-v") {
                    +chapter.heading
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
