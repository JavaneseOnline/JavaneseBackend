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
        val icon: String,
        val subtitle: String,
        val headMarkup: Html,
        val heading: String,
        val bodyMarkup: Html,
        val beforeBodyEndMarkup: Html,
        val lastModified: LocalDateTime,
        val sortIndex: Int
) {
    enum class Magic { // todo: re-think arch & eliminate
        Index, Courses, Tree, Articles, CodeReview
    }
}

object PageTable : Table<Page, Uuid>("pages"), VersionedWithTimestamp {

    val Id by idCol(Page::id)
    val UrlSegment by urlSegmentCol(Page::urlSegment)
    val Magic by col(Page::magic, name = "magic", default = Page.Magic.Index)
    val MetaTitle by metaTitleCol(Page::meta)
    val MetaDescription by metaDescriptionCol(Page::meta)
    val MetaKeywords by metaKeywordsCol(Page::meta)
    val Icon by col(Page::icon, name = "icon")
    val Subtitle by col(Page::subtitle, name = "subtitle")
    val HeadMarkup by col(Page::headMarkup, name = "headMarkup")
    val Heading by headingCol(Page::heading)
    val BodyMarkup by col(Page::bodyMarkup, name = "bodyMarkup")
    val BeforeBodyEndMarkup by col(Page::beforeBodyEndMarkup, name = "beforeBodyEndMarkup")
    val LastModified by lastModifiedCol(Page::lastModified)
    val SortIndex by sortIndexCol(Page::sortIndex)

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
            icon = value of Icon,
            subtitle = value of Subtitle,
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            headMarkup = value of HeadMarkup,
            beforeBodyEndMarkup = value of BeforeBodyEndMarkup,
            lastModified = value of LastModified,
            sortIndex = value of SortIndex
    )

}

class PageDao(
        session: Session
) : AbstractDao<Page, Uuid>(session, PageTable, PageTable.Id.property) {

    private val tableName = PageTable.name
    private val urlPathComponentColName = PageTable.UrlSegment.name
    private val magicColName = PageTable.Magic.name
    override val defaultOrder: Map<Column<Page, *>, OrderByDirection> = mapOf(PageTable.SortIndex to OrderByDirection.ASC)

    fun findAllSecondary(): List<Page> =
            session.select(
                    sql = """SELECT * FROM $tableName where "$urlPathComponentColName" != '' ORDER BY "sortIndex" """,
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
	"icon" varchar(64) NOT NULL,
	"subtitle" varchar(256) NOT NULL,
	"headMarkup" text NOT NULL,
	"heading" varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"beforeBodyEndMarkup" text NOT NULL,
	"lastModified" timestamp NOT NULL,
	"sortIndex" int4 NOT NULL,
	CONSTRAINT pages_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
CREATE UNIQUE INDEX pages_urlsegment_idx ON public.pages ("urlSegment") ;
 */
