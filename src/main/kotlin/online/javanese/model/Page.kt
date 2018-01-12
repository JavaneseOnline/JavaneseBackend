package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class Page( // todo: introduce BasicInfo
        val id: Uuid,
        val urlSegment: String,
        val magic: Magic,
        val meta: Meta,
        val headMarkup: Html,
        val heading: String,
        val bodyMarkup: Html,
        val beforeBodyEndMarkup: Html,
        val lastModified: LocalDateTime
) {
    enum class Magic { // todo: re-think arch & eliminate
        Index, Tree, Articles, CodeReview
    }
}

object PageTable : Table<Page, Uuid>("pages"), VersionedWithTimestamp {

    val Id by idCol(Page::id)
    val UrlSegment by urlSegmentCol(Page::urlSegment)
    val Magic by col(Page::magic, name = "magic", default = Page.Magic.Index)
    val MetaTitle by metaTitleCol(Page::meta)
    val MetaDescription by metaDescriptionCol(Page::meta)
    val MetaKeywords by metaKeywordsCol(Page::meta)
    val HeadMarkup by col(Page::headMarkup, name = "headMarkup")
    val Heading by headingCol(Page::heading)
    val BodyMarkup by col(Page::bodyMarkup, name = "bodyMarkup")
    val BeforeBodyEndMarkup by col(Page::beforeBodyEndMarkup, name = "beforeBodyEndMarkup")
    val LastModified by lastModifiedCol(Page::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Page, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Page>): Page = Page(
            id = value of Id,
            urlSegment = value of UrlSegment,
            magic = value of Magic,
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            headMarkup = value of HeadMarkup,
            beforeBodyEndMarkup = value of BeforeBodyEndMarkup,
            lastModified = value of LastModified
    )

}

class PageDao(
        session: Session
) : AbstractDao<Page, Uuid>(session, PageTable, PageTable.Id.property) {

    private val tableName = PageTable.name
    private val urlPathComponentColName = PageTable.UrlSegment.name
    private val magicColName = PageTable.Magic.name

    fun findAll(): List<Page> =
            session.select(
                    sql = """SELECT * FROM $tableName""",
                    mapper = PageTable.rowMapper()
            )

    fun findByUrlSegment(segment: String) =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlPathComponentColName" = :segment""",
                    parameters = mapOf("segment" to segment),
                    mapper = PageTable.rowMapper()
            ).singleOrNull()

    fun findByMagic(magic: Page.Magic) =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$magicColName" = :magic""",
                    parameters = mapOf("magic" to magic.name),
                    mapper = PageTable.rowMapper()
            ).singleOrNull()

}

/*
CREATE TABLE public.pages (
	"id" uuid NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"magic" varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"headMarkup" text NOT NULL,
	"heading" varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"beforeBodyEndMarkup" text NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT pages_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
CREATE UNIQUE INDEX pages_urlsegment_idx ON public.pages ("urlSegment") ;
 */
