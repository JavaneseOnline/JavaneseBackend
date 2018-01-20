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
        val published: Boolean,
        val vkPostInfo: VkPostInfo?,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val linkText: String,
            val description_duplicatesMetaDescription: String,
            val urlSegment: String,
            val createdAt: LocalDateTime,
            val lastModified: LocalDateTime,
            val pinned: Boolean
    )

}


object ArticleTable : Table<Article, Uuid>("articles") {

    val Id by idCol(Article.BasicInfo::id, Article::basicInfo)
    val LinkText by linkTextCol(Article.BasicInfo::linkText, Article::basicInfo)
    val UrlSegment by urlSegmentCol(Article.BasicInfo::urlSegment, Article::basicInfo)

    val MetaTitle by metaTitleCol(Article::meta)
    val MetaDescription by metaDescriptionCol(Article::meta)
    val MetaKeywords by metaKeywordsCol(Article::meta)

    val Heading by headingCol(Article::heading)
    val BodyMarkup by col(Article::bodyMarkup, name = "bodyMarkup")
    val Published by col(Article::published, name = "published")

    val VkPostId by vkPostIdCol(Article::vkPostInfo)
    val VkPostHash by vkPostHashCol(Article::vkPostInfo)

    val CreatedAt by col(Article.BasicInfo::createdAt, Article::basicInfo, name = "createdAt")
    val LastModified by lastModifiedCol(Article::lastModified)
    val Pinned by col(Article.BasicInfo::pinned, Article::basicInfo, name = "pinned")


    override fun idColumns(id: Uuid): Set<Pair<Column<Article, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article>): Article = Article(
            basicInfo = Article.BasicInfo(
                    id = value of Id,
                    linkText = value of LinkText,
                    description_duplicatesMetaDescription = value of MetaDescription,
                    urlSegment = value of UrlSegment,
                    lastModified = value of LastModified,
                    createdAt = value of CreatedAt,
                    pinned = value of Pinned
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            published = value of Published,
            vkPostInfo = VkPostInfo.fromComponentsOrNull(
                    id = value of VkPostId,
                    hash = value of VkPostHash
            ),
            lastModified = value of LastModified
    )

}


object BasicArticleInfoTable : Table<Article.BasicInfo, Uuid>("articles") {

    val Id by idCol(Article.BasicInfo::id)
    val LinkText by linkTextCol(Article.BasicInfo::linkText)
    val MetaDescription by col(Article.BasicInfo::description_duplicatesMetaDescription, name = "metaDescription")
    val UrlSegment by urlSegmentCol(Article.BasicInfo::urlSegment)
    val CreatedAt by col(Article.BasicInfo::createdAt, name = "createdAt")
    val LastModified by lastModifiedCol(Article.BasicInfo::lastModified)
    val Pinned by col(Article.BasicInfo::pinned, name = "pinned")

    override fun idColumns(id: Uuid): Set<Pair<Column<Article.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Article.BasicInfo>): Article.BasicInfo = Article.BasicInfo(
            id = value of Id,
            linkText = value of LinkText,
            description_duplicatesMetaDescription = value of MetaDescription,
            urlSegment = value of UrlSegment,
            createdAt = value of CreatedAt,
            lastModified = value of LastModified,
            pinned = value of Pinned
    )

}


class ArticleDao(
        session: Session
) : AbstractDao<Article, Uuid>(session, ArticleTable, ArticleTable.Id.property) {

    private val tableName = ArticleTable.name
    private val basicCols = """"id", "linkText", "metaDescription", "urlSegment", "createdAt", "lastModified", "pinned" """
    private val urlComponentColName = ArticleTable.UrlSegment.name
    private val publishedColName = ArticleTable.Published.name
    private val createdAtColName = ArticleTable.CreatedAt.name
    private val pinnedColName = ArticleTable.Pinned.name

    override val defaultOrder: Map<Column<Article, *>, OrderByDirection> =
            mapOf(ArticleTable.Pinned to OrderByDirection.DESC, ArticleTable.CreatedAt to OrderByDirection.DESC)

    private val naturalOrder = """ ORDER BY "$pinnedColName" DESC, "$createdAtColName" DESC """

    fun findAllBasicPublished(): List<Article.BasicInfo> =
            session.select(
                    sql = """SELECT $basicCols FROM "$tableName" WHERE "$publishedColName" = true $naturalOrder""",
                    mapper = BasicArticleInfoTable.rowMapper()
            )

    fun findByUrlSegment(segment: String): Article? =
            session.select(
                    sql = """SELECT * FROM "$tableName" WHERE "$urlComponentColName" = :segment LIMIT 1""",
                    parameters = mapOf("segment" to segment),
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
CREATE UNIQUE INDEX articles_urlsegment_idx ON public.articles ("urlSegment") ;
 */
