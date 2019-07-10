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
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        taskDao: TaskDao,
        articleDao: ArticleDao,
        layout: Layout,
        indexPage: (Page) -> Layout.Page,
        coursesPage: (idx: Page, tr: Page, List<Course.BasicInfo>) -> Layout.Page,
        treePage: (idx: Page, tr: Page, Courses) -> Layout.Page,
        articlesPage: (idx: Page, ar: Page, List<Article.BasicInfo>) -> Layout.Page,
        codeReview: (idx: Page, cr: Page, call: ApplicationCall) -> Layout.Page
): suspend (ApplicationCall, Page) -> Unit = { call, page ->

    val htmlPage = when (page.magic) {
        Index -> indexPage(page)
        Courses -> coursesPage(pageDao.findByMagic(Index)!!, page, courseDao.findAllBasicSorted())
        Tree -> treePage(pageDao.findByMagic(Index)!!, page, courses(courseDao, chapterDao, lessonDao, taskDao))
        Articles -> articlesPage(pageDao.findByMagic(Index)!!, page, articleDao.findAllBasicPublished())
        CodeReview -> codeReview(pageDao.findByMagic(Index)!!, page, call)
    }

    call.respondHtml {
        layout(this, htmlPage)
    }

}
