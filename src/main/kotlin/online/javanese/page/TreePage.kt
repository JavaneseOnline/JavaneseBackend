package online.javanese.page

import kotlinx.html.*
import online.javanese.handler.Courses
import online.javanese.link.HtmlBlock
import online.javanese.link.Link
import online.javanese.link.LinkWithFragment
import online.javanese.locale.Language
import online.javanese.model.*


class TreePage(
        private val page: Page,
        private val courses: Courses,
        private val language: Language,
        private val courseLink: Link<Course.BasicInfo, *>,
        private val chapterLink: Link<Chapter.BasicInfo, *>,
        private val lessonLink: Link<Lesson.BasicInfo, *>,
        private val taskLink: Link<Task.BasicInfo, *>,
        private val beforeContent: HtmlBlock,
        private val lessonsLink: LinkWithFragment<Page, *>,
        private val tasksLink: LinkWithFragment<Page, *>
): Layout.Page {

    override val meta: Meta get() = page.meta

    override fun additionalHeadMarkup(head: HEAD) = Unit

    override fun bodyMarkup(body: BODY) = with(body) {
        contentCardMain {

            beforeContent.render(this, "")

            h1(classes = "content-padding-v") {
                +page.heading
            }

            unsafe {
                +page.bodyMarkup
            }

            tabs {
                val lessonsFrag = lessonsLink.fragment(page)
                val tasksFrag = tasksLink.fragment(page)

                tabBar {
                    tabLink(lessonsFrag, language.lessonsTreeTab, true)
                    tabLink(tasksFrag, language.tasksTreeTab)
                }

                tabPanelNav(lessonsFrag, true) {
                    coursesTree(courses, courseLink, chapterLink, lessonLink, taskLink, TreeMode.Lessons)
                }

                tabPanelNav(tasksFrag) {
                    coursesTree(courses, courseLink, chapterLink, lessonLink, taskLink, TreeMode.Tasks)
                }
            }
        }
    }

    override fun scripts(body: BODY) = Unit

}
