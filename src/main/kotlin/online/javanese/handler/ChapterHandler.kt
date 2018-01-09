package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.page.Layout


fun ChapterHandler(
        pageDao: PageDao,
        tree: List<CourseTree>,
        layout: Layout,
        page: (idxPage: Page, treePage: Page, Course, Chapter, ChapterTree, previous: ChapterTree?, next: ChapterTree?) -> Layout.Page
): suspend (Course, Chapter, ApplicationCall) -> Unit = { course, chapter, call ->

    val idxPage = pageDao.findByMagic(Page.Magic.Index)!!
    val treePage = pageDao.findByMagic(Page.Magic.Tree)!!

    // fixme: should stop keeping whole graph in memory
    val chapters = tree
            .first { it.id == course.basicInfo.id }
            .chapters

    val chapterTree = chapters
            .first { it.id == chapter.basicInfo.id }

    val idx = chapters.indexOf(chapterTree)
    val prev = (idx - 1).let { if (it < 0) null else chapters[it] }
    val next = (idx + 1).let { if (it < chapters.size) chapters[it] else null }

    call.respondHtml {
        layout(this, page(idxPage, treePage, course, chapter, chapterTree, prev, next))
    }

}