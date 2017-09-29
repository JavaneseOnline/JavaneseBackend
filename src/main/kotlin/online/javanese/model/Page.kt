package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.DefaultUuid
import online.javanese.UuidConverter
import java.time.LocalDateTime
import java.util.*

class Page(
        val id: UUID,
        val urlPathComponent: String, // was 'url'
        val magic: Magic,
        val meta: Meta,
        val headMarkup: String, // was 'head'
        val h1: String,
        val contentMarkup: String, // was 'markup', todo: display as HTML in admin panel
        val beforeBodyEndMarkup: String, // was 'beforeBodyEnd'
        val lastModified: LocalDateTime
) {
    enum class Magic {
        Index, Tree, Articles, CodeReview
    }
}

// pages (id, h1, magic, markup, meta_description, meta_keywords, title, url, before_body_end, head, last_modified)
object PagesTable : Table<Page, UUID>("pages"), VersionedWithTimestamp {

    private val Id by col(Page::id, name = "id", id = true, default = DefaultUuid, converter = UuidConverter)
    private val UrlPathComponent by col(Page::urlPathComponent, name = "urlPathComponent")
    private val Magic by col(Page::magic, name = "magic", default = Page.Magic.Index)
    private val Title by col(Meta::title, Page::meta, name = "title")
    private val MetaDescription by col(Meta::description, Page::meta, name = "metaDescription")
    private val MetaKeywords by col(Meta::keywords, Page::meta, name = "metaKeywords")
    private val HeadMarkup by col(Page::headMarkup, name = "headMarkup")
    private val H1 by col(Page::h1, name = "h1")
    private val ContentMarkup by col(Page::contentMarkup, name = "contentMarkup")
    private val BeforeBodyEndMarkup by col(Page::beforeBodyEndMarkup, name = "beforeBodyEndMarkup")
    private val LastModified by col(Page::lastModified, name = "lastModified", version = true)

    override fun idColumns(id: UUID): Set<Pair<Column<Page, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Page>): Page = Page(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            magic = value of Magic,
            meta = Meta(
                    title = value of Title,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            h1 = value of H1,
            contentMarkup = value of ContentMarkup,
            headMarkup = value of HeadMarkup,
            beforeBodyEndMarkup = value of BeforeBodyEndMarkup,
            lastModified = value of LastModified
    )

}

class PageDao(
        session: Session,
        private val baseDao: AbstractDao<Page, UUID> = object : AbstractDao<Page, UUID>(session, PagesTable, Page::id) {}
) : Dao<Page, UUID> by baseDao {

    fun findByUrlPathComponent(component: String) =
            baseDao.session.select(
                    sql = "SELECT * FROM ${baseDao.table.name} WHERE \"urlPathComponent\" = :component",
                    parameters = mapOf("component" to component),
                    mapper = baseDao.table.rowMapper()
            ).singleOrNull()

}

/*
CREATE TABLE public.pages (
	id uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	magic varchar(64) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	title varchar(256) NOT NULL,
	h1 varchar(256) NOT NULL,
	"headMarkup" text NOT NULL,
	"contentMarkup" text NOT NULL,
	"beforeBodyEndMarkup" text NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT pages_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
