package online.javanese.handler

import com.github.andrewoma.kwery.mapper.Dao
import com.redfin.sitemapgenerator.WebSitemapGenerator
import com.redfin.sitemapgenerator.WebSitemapUrl
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.krud.kwery.Uuid
import online.javanese.model.*
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
        urlOfCodeReview: (Page, CodeReview) -> String,
        tree: List<CourseTree>,
        pageDao: PageDao,
        courseDao: CourseDao,
        chapterDao: ChapterDao,
        lessonDao: LessonDao,
        articleDao: ArticleDao,
        codeReviewDao: Dao<CodeReview, Uuid>
): suspend (ApplicationCall) -> Unit = { call ->

    // fixme: move [lastModified] to [BasicInfo]s

    val wsg = WebSitemapGenerator(siteUrl)

    val sb = StringBuilder(siteUrl)

    val pages = pageDao.findAll()

    wsg.addAll(sb, pages, urlOfPage, PageTable.LastModified.property)

    wsg.addAll(sb, tree, urlOfCourseTree, { courseDao.findById(it.id)!!.lastModified }) { course ->
        wsg.addAll(sb, course.chapters, urlOfChapterTree, { chapterDao.findById(it.id)!!.lastModified }) { chapter ->
            wsg.addAll(sb, chapter.lessons, urlOfLessonTree, { lessonDao.findById(it.id)!!.lastModified })
        }
    }

    val articlesPage = pages.first { it.magic == Page.Magic.Articles }
    wsg.addAll(sb, articleDao.findAllBasicPublished(),
            { urlOfArticle(articlesPage, it) }, Article.BasicInfo::lastModified)

    val codeReviewsPage = pages.first { it.magic == Page.Magic.Articles }
    wsg.addAll(sb, codeReviewDao.findAll(),
            { urlOfCodeReview(codeReviewsPage, it) }, CodeReviewTable.LastModified.property)

    val strs = wsg.writeAsStrings()
    call.respondText(strs.joinToString("\n"), ContentType.Text.Xml)
}

private inline fun <T> WebSitemapGenerator.addAll(
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
        addUrl(sb.toString(), lastMod)
        sb.setLength(len)

        onEach(item)
    }
}

private val zone = ZoneId.systemDefault()
private fun WebSitemapGenerator.addUrl(url: String, lastMod: LocalDateTime) {
    addUrl(WebSitemapUrl(WebSitemapUrl.Options(url).also { it.lastMod(Date.from(lastMod.atZone(zone).toInstant())) }))
}
