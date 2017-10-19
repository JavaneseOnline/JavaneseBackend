package online.javanese.handler

import com.redfin.sitemapgenerator.WebSitemapGenerator
import com.redfin.sitemapgenerator.WebSitemapUrl
import online.javanese.model.*
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun SitemapHandler(
        siteUrl: String,
        urlOfPage: (Page) -> String,
        urlOfCourseTree: (CourseTree) -> String,
        urlOfChapterTree: (ChapterTree) -> String,
        urlOfLessonTree: (LessonTree) -> String,
        urlOfArticle: (Page, Article.BasicInfo) -> String,
        tree: List<CourseTree>,
        pageDao: PageDao,
        courseDao: CourseDao,
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        articleDao: ArticleDao
): suspend (ApplicationCall) -> Unit = { call ->

    val wsg = WebSitemapGenerator(siteUrl)

    val sb = StringBuilder(siteUrl)

    val pages = pageDao.findAll()

    addAll(wsg, sb, pages, urlOfPage, Page::lastModified)

    addAll(wsg, sb, tree, urlOfCourseTree, { courseDao.findById(it.id)!!.lastModified }) { course ->
        addAll(wsg, sb, course.chapters, urlOfChapterTree, { chapterDao.findById(it.id)!!.lastModified }) { chapter ->
            addAll(wsg, sb, chapter.lessons, urlOfLessonTree, { lessonDao.findById(it.id)!!.lastModified })
        }
    }

    val articlesPage = pages.first { it.magic == Page.Magic.Articles }
    addAll(wsg, sb, articleDao.findAllBasicPublishedOrderBySortIndex(),
            { urlOfArticle(articlesPage, it) }, Article.BasicInfo::lastModified)

    val strs = wsg.writeAsStrings()
    call.respondText(strs.joinToString("\n"), ContentType.Text.Xml)
}

private inline fun <T> addAll(
        wsg: WebSitemapGenerator,
        sb: StringBuilder,
        items: List<T>,
        urlOfT: (T) -> String,
        lastModOfT: (T) -> LocalDateTime,
        onEach: (T) -> Unit = { }
) {
    items.forEach { item ->
        val len = sb.length
        sb.append(urlOfT(item))
        val lastMod = lastModOfT(item)
        wsg.addUrl(sb.toString(), lastMod)
        sb.setLength(len)

        onEach(item)
    }
}

private val zone = ZoneId.systemDefault()
private fun WebSitemapGenerator.addUrl(url: String, lastMod: LocalDateTime) {
    addUrl(WebSitemapUrl(WebSitemapUrl.Options(url).also { it.lastMod(Date.from(lastMod.atZone(zone).toInstant())) }))
}
