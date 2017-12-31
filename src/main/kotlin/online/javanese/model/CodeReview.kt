package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.AbstractDao
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Table
import com.github.andrewoma.kwery.mapper.Value
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class CodeReview(
        val id: Uuid,
        val urlSegment: String,
        val meta: Meta,
        val senderName: String,
        val problemStatement: String,
        val reviewMarkup: Html,
        val datePublished: LocalDateTime,
        val lastModified: LocalDateTime
)

object CodeReviewTable : Table<CodeReview, Uuid>("codeReviews") {

    val Id
            by idCol(CodeReview::id)

    val UrlSegment
            by urlSegmentCol(CodeReview::urlSegment)

    val MetaTitle
            by metaTitleCol(CodeReview::meta)

    val MetaDescription
            by metaDescriptionCol(CodeReview::meta)

    val MetaKeywords
            by metaKeywordsCol(CodeReview::meta)

    val SenderName
            by col(CodeReview::senderName, name = "senderName")

    val ProblemStatement
            by col(CodeReview::problemStatement, name = "problemStatement")

    val ReviewMarkup
            by col(CodeReview::reviewMarkup, name = "review")

    val DatePublished
            by col(CodeReview::datePublished, name = "datePublished")

    val LastModified
            by col(CodeReview::lastModified, name = "lastModified")

    override fun idColumns(id: Uuid): Set<Pair<Column<CodeReview, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<CodeReview>): CodeReview = CodeReview(
            id = value of Id,
            urlSegment = value of UrlSegment,
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            senderName = value of SenderName,
            problemStatement = value of ProblemStatement,
            reviewMarkup = value of ReviewMarkup,
            datePublished = value of DatePublished,
            lastModified = value of LastModified
    )

}

class CodeReviewDao(
        session: Session
) : AbstractDao<CodeReview, Uuid>(session, CodeReviewTable, CodeReviewTable.Id.property) {

    fun findByUrlSegment(segment: String): CodeReview? =
            session.select(
                    sql = """SELECT *
                        |FROM "${table.name}"
                        |WHERE "${CodeReviewTable.UrlSegment.name}" = :segment
                        |LIMIT 1""".trimMargin(),

                    parameters = mapOf("segment" to segment),
                    mapper = CodeReviewTable.rowMapper()
            ).firstOrNull()

}

/*
CREATE TABLE public."codeReviews" (
	"id" uuid NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"senderName" varchar(256) NOT NULL,
	"problemStatement" text NOT NULL,
	"review" text NOT NULL,
	"datePublished" timestamp NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT codereviews_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
