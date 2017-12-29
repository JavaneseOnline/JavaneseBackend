package online.javanese.route

import io.ktor.application.ApplicationCall
import online.javanese.exception.NotFoundException
import online.javanese.model.*


fun TwoPartsRoute(
        pageDao: PageDao,
        articleDao: ArticleDao,
        courseDao: CourseDao,
        chapterDao: ChapterDao,
        codeReviewDao: CodeReviewDao,
        articleHandler: suspend (articlesPage: Page, article: Article, call: ApplicationCall) -> Unit,
        chapterHandler: suspend (Course, Chapter, ApplicationCall) -> Unit,
        codeReviewHandler: suspend (Page, CodeReview, ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String) -> Unit = f@ { call, first, second ->

    pageDao.findByUrlPathComponent(first)?.let { page ->
        when (page.magic) {
            Page.Magic.Articles -> articleDao.findByUrlComponent(second)?.let { article ->
                return@f articleHandler(page, article, call)
            }
            Page.Magic.CodeReview -> codeReviewDao.findByUrlSegment(second)?.let { codeReview ->
                return@f codeReviewHandler(page, codeReview, call)
            }
        }
        Unit
    }

    courseDao.findByUrlComponent(first)?.let { course -> // todo: this lookup may be optimized
        chapterDao.findByUrlComponent(second)?.let { chapter ->
            return@f chapterHandler(course, chapter, call)
        }
    }

    throw NotFoundException("neither article nor course exist for address /$first/$second/")
}
