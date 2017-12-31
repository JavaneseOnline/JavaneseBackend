package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime

class Article(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val heading: String,
        val bodyMarkup: Html,
        @Deprecated(message = "natural order changed")
        val sortIndex: Int,
        val published: Boolean,
        val vkPostInfo: VkPostInfo?,
        val createdAt: LocalDateTime,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val linkText: String,
            val urlPathComponent: String,
            val lastModified: LocalDateTime,
            val pinned: Boolean
    )

    class VkPostInfo(
            val id: String,
            val hash: String
    )

    internal val vkPostIdOrNull get() = vkPostInfo?.id ?: ""
    internal val vkPostHashOrNull get() = vkPostInfo?.hash ?: ""

}

object ArticleTable : Table<Article, Uuid>("articles") {

    val Id by idCol(Article.BasicInfo::id, Article::basicInfo)
    val LinkText by linkTextCol(Article.BasicInfo::linkText, Article::basicInfo)
    val UrlPathComponent by urlSegmentCol(Article.BasicInfo::urlPathComponent, Article::basicInfo)

    val MetaTitle by metaTitleCol(Article::meta)
    val MetaDescription by metaDescriptionCol(Article::meta)
    val MetaKeywords by metaKeywordsCol(Article::meta)

    val Heading by col(Article::heading, name = "heading")
    val BodyMarkup by col(Article::bodyMarkup, name = "bodyMarkup")
    val SortIndex by sortIndexCol(Article::sortIndex)
    val Published by col(Article::published, name = "published")

    val VkPostId by col(Article::vkPostIdOrNull, name = "vkPostId")
    val VkPostHash by col(Article::vkPostHashOrNull, name = "vkPostHash")

    val CreatedAt by col(Article::createdAt, name = "createdAt")
    val LastModified by lastModifiedCol(Article::lastModified)
    val Pinned by col(Article.BasicInfo::pinned, Article::basicInfo, name = "pinned")


    override fun idColumns(id: Uuid): Set<Pair<Column<Article, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article>): Article = Article(
            basicInfo = Article.BasicInfo(
                    id = value of Id,
                    linkText = value of LinkText,
                    urlPathComponent = value of UrlPathComponent,
                    lastModified = value of LastModified,
                    pinned = value of Pinned
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            sortIndex = value of SortIndex,
            published = value of Published,
            vkPostInfo = vkPostInfoOrNull(
                    vkPostId = value of VkPostId,
                    vkPostHash = value of VkPostHash
            ),
            createdAt = value of CreatedAt,
            lastModified = value of LastModified
    )

    private fun vkPostInfoOrNull(vkPostId: String, vkPostHash: String): Article.VkPostInfo? {
        if (vkPostId.isBlank()) return null
        if (vkPostHash.isBlank()) return null
        return Article.VkPostInfo(
                id = vkPostId,
                hash = vkPostHash
        )
    }


}

private object ArticleBasicInfoTable : Table<Article.BasicInfo, Uuid>("articles") {

    val Id by idCol(Article.BasicInfo::id)
    val LinkText by linkTextCol(Article.BasicInfo::linkText)
    val UrlPathComponent by urlSegmentCol(Article.BasicInfo::urlPathComponent)
    val LastModified by lastModifiedCol(Article.BasicInfo::lastModified)
    val Pinned by col(Article.BasicInfo::pinned, name = "pinned")

    override fun idColumns(id: Uuid): Set<Pair<Column<Article.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article.BasicInfo>): Article.BasicInfo = Article.BasicInfo(
            id = value of Id,
            linkText = value of LinkText,
            urlPathComponent = value of UrlPathComponent,
            lastModified = value of LastModified,
            pinned = value of Pinned
    )

}

class ArticleDao(
        session: Session
) : AbstractDao<Article, Uuid>(session, ArticleTable, ArticleTable.Id.property) {

    private val tableName = ArticleTable.name
    private val basicCols = """"id", "linkText", "urlSegment", "lastModified", "pinned" """
    private val urlComponentColName = ArticleTable.UrlPathComponent.name
    private val publishedColName = ArticleTable.Published.name
    private val createdAtColName = ArticleTable.CreatedAt.name
    private val pinnedColName = ArticleTable.Pinned.name

    override val defaultOrder: Map<Column<Article, *>, OrderByDirection> =
            mapOf(ArticleTable.Pinned to OrderByDirection.DESC, ArticleTable.CreatedAt to OrderByDirection.DESC)

    private val naturalOrder = """ ORDER BY "$pinnedColName" DESC, "$createdAtColName" DESC """

    fun findAllBasicPublished(): List<Article.BasicInfo> =
            session.select(
                    sql = """SELECT $basicCols
                        |FROM "$tableName"
                        |WHERE "$publishedColName" = true
                        |$naturalOrder""".trimMargin(),
                    mapper = ArticleBasicInfoTable.rowMapper()
            )

    fun findAllPublished(): List<Article> =
            session.select(
                    sql = """SELECT *
                        |FROM "$tableName"
                        |WHERE "$publishedColName" = true
                        |$naturalOrder""".trimMargin(),
                    mapper = ArticleTable.rowMapper()
            )

    fun findByUrlComponent(component: String): Article? =
            session.select(
                    sql = """SELECT *
                        |FROM "$tableName"
                        |WHERE "$urlComponentColName" = :component
                        |LIMIT 1""".trimMargin(),
                    parameters = mapOf("component" to component),
                    mapper = ArticleTable.rowMapper()
            ).firstOrNull()

}

/*
CREATE TABLE public.articles (
	id uuid NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"heading" varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"published" bool NOT NULL,
	"vkPostId" varchar(64) NOT NULL,
	"vkPostHash" varchar(64) NOT NULL,
	"createdAt" timestamp NOT NULL,
	"lastModified" timestamp NOT NULL,
	"pinned" bool NOT NULL,
	CONSTRAINT articles_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
