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
        articleHandler: suspend (idx: Page, articles: Page, article: Article, call: ApplicationCall) -> Unit,
        chapterHandler: suspend (Course.BasicInfo, Chapter, ApplicationCall) -> Unit,
        codeReviewHandler: suspend (idx: Page, cr: Page, CodeReview, ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String) -> Unit = f@ { call, first, second ->

    pageDao.findByUrlSegment(first)?.let { page ->
        when (page.magic) {
            Page.Magic.Articles -> articleDao.findByUrlSegment(second)?.let { article ->
                return@f articleHandler(pageDao.findByMagic(Page.Magic.Index)!!, page, article, call)
            }
            Page.Magic.CodeReview -> codeReviewDao.findByUrlSegment(second)?.let { codeReview ->
                return@f codeReviewHandler(pageDao.findByMagic(Page.Magic.Index)!!, page, codeReview, call)
            }
            else -> { /* pass through, nothing special */ }
        }
        Unit
    }

    courseDao.findBasicByUrlSegment(first)?.let { course -> // todo: this lookup may be optimized
        chapterDao.findByUrlSegment(course.id, second)?.let { chapter ->
            return@f chapterHandler(course, chapter, call)
        }
    }

    throw NotFoundException("neither article nor course exist for address /$first/$second/")
}
