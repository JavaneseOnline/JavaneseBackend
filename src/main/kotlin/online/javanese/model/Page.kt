package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.Uuid
import java.time.LocalDateTime

class Page(
        val id: Uuid,
        val urlPathComponent: String,
        val magic: Magic,
        val meta: Meta,
        val headMarkup: Html,
        val h1: String,
        val contentMarkup: Html, // todo: display as HTML in admin panel
        val beforeBodyEndMarkup: Html,
        val lastModified: LocalDateTime
) {
    enum class Magic { // todo: transform to sealed class
        Index, Tree, Articles, CodeReview
    }
}

private object PagesTable : Table<Page, Uuid>("pages"), VersionedWithTimestamp {

    val Id by idCol(Page::id)
    val UrlPathComponent by urlPathComponentCol(Page::urlPathComponent)
    val Magic by col(Page::magic, name = "magic", default = Page.Magic.Index)
    val MetaTitle by metaTitleCol(Page::meta)
    val MetaDescription by metaDescriptionCol(Page::meta)
    val MetaKeywords by metaKeywordsCol(Page::meta)
    val HeadMarkup by col(Page::headMarkup, name = "headMarkup")
    val H1 by col(Page::h1, name = "h1")
    val ContentMarkup by col(Page::contentMarkup, name = "contentMarkup")
    val BeforeBodyEndMarkup by col(Page::beforeBodyEndMarkup, name = "beforeBodyEndMarkup")
    val LastModified by lastModifiedCol(Page::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Page, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Page>): Page = Page(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            magic = value of Magic,
            meta = Meta(
                    title = value of MetaTitle,
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

internal class PageDao(
        private val session: Session,
        private val baseDao: Dao<Page, Uuid> = object : AbstractDao<Page, Uuid>(session, PagesTable, Page::id) {}
) : Dao<Page, Uuid> by baseDao {

    private val tableName = PagesTable.name
    private val urlPathComponentColName = PagesTable.UrlPathComponent.name

    fun findByUrlPathComponent(component: String) =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlPathComponentColName" = :component""",
                    parameters = mapOf("component" to component),
                    mapper = PagesTable.rowMapper()
            ).singleOrNull()

}

/*
CREATE TABLE public.pages (
	id uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	magic varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"headMarkup" text NOT NULL,
	h1 varchar(256) NOT NULL,
	"contentMarkup" text NOT NULL,
	"beforeBodyEndMarkup" text NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT pages_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
