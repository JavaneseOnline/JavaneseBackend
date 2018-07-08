package online.javanese.handler

import com.github.andrewoma.kwery.mapper.Dao
import com.redfin.sitemapgenerator.WebSitemapGenerator
import com.redfin.sitemapgenerator.WebSitemapUrl
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import online.javanese.krud.kwery.Uuid
import online.javanese.model.*
import online.javanese.link.Link
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


fun SitemapHandler(
        siteUrl: String,
        pageLink: Link<Page, *>,
        courseLink: Link<Course.BasicInfo, *>,
        chapterLink: Link<Chapter.BasicInfo, *>,
        lessonLink: Link<Lesson.BasicInfo, *>,
        articleLink: Link<Article.BasicInfo, *>,
        codeReviewLink: Link<CodeReview, *>,
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

    wsg.addAll(sb, pages, pageLink::url, PageTable.LastModified.property)

    wsg.addAll(sb, courseDao.findAllBasicSorted(), courseLink::url, { courseDao.findById(it.id)!!.lastModified }) { course ->
        wsg.addAll(sb, chapterDao.findAllBasicSorted(course.id), chapterLink::url, { chapterDao.findById(it.id)!!.lastModified }) { chapter ->
            wsg.addAll(sb, lessonDao.findAllBasicSorted(chapter.id), lessonLink::url, { lessonDao.findById(it.id)!!.lastModified })
        }
    }

    wsg.addAll(sb, articleDao.findAllBasicPublished(), articleLink::url, Article.BasicInfo::lastModified)

    wsg.addAll(sb, codeReviewDao.findAll(), codeReviewLink::url, CodeReviewTable.LastModified.property)

    val strs = wsg.writeAsStrings()
    call.respondText(strs.joinToString("\n"), ContentType.Text.Xml)
}

private /*inline Back-end (JVM) internal error */ fun <T> WebSitemapGenerator.addAll(
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
