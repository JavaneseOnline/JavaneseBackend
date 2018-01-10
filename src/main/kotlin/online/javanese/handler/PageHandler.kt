package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.html.respondHtml
import online.javanese.model.*
import online.javanese.model.Page.Magic.*
import online.javanese.model.Page.Magic.CodeReview
import online.javanese.page.Layout


fun PageHandler(
        pageDao: PageDao,
        courseDao: CourseDao,
        articleDao: ArticleDao,
        layout: Layout,
        indexPage: (Page) -> Layout.Page,
        treePage: (idx: Page, tr: Page, List<CourseTree>) -> Layout.Page,
        articlesPage: (idx: Page, ar: Page, List<Article.BasicInfo>) -> Layout.Page,
        codeReview: (idx: Page, cr: Page) -> Layout.Page
): suspend (ApplicationCall, Page) -> Unit = { call, page ->

    val htmlPage = when (page.magic) {
        Index -> indexPage(page)
        Tree -> treePage(pageDao.findByMagic(Index)!!, page, courseDao.findTreeSortedBySortIndex())
        Articles -> articlesPage(pageDao.findByMagic(Index)!!, page, articleDao.findAllBasicPublished())
        CodeReview -> codeReview(pageDao.findByMagic(Index)!!, page)
    }

    call.respondHtml {
        layout(this, htmlPage)
    }

}
