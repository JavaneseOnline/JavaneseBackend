package online.javanese.route

import online.javanese.exception.NotFoundException
import online.javanese.model.*
import online.javanese.repository.ChapterRepository
import online.javanese.repository.CourseRepository
import online.javanese.repository.PageRepository
import org.jetbrains.ktor.application.ApplicationCall

fun TwoPartsRoute(
        pageRepo: PageRepository,
        articleDao: ArticleDao,
        courseRepo: CourseRepository,
        chapterRepo: ChapterRepository,
        articleHandler: suspend (articlesPage: Page, article: Article, call: ApplicationCall) -> Unit,
        chapterHandler: suspend (Course, Chapter, ApplicationCall) -> Unit
): suspend (ApplicationCall, String, String) -> Unit = f@ { call, first, second ->

    pageRepo.findByUrlPathComponent(first)?.let { page ->
        if (page.magic == Page.Magic.Articles) {
            articleDao.findByUrlComponent(second)?.let { article ->
                return@f articleHandler(page, article, call)
            }
        }
    }

    courseRepo.findByUrlComponent(first)?.let { course -> // todo: this lookup may be optimized
        chapterRepo.findByUrlComponent(second)?.let { chapter ->
            return@f chapterHandler(course, chapter, call)
        }
    }

    throw NotFoundException("neither article nor course exist for address /$first/$second/")
}
