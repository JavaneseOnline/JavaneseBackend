package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class Chapter(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val heading: String,
        val description: Html,
        val sortIndex: Int,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val courseId: Uuid,
            val urlSegment: String,
            val linkText: String
    )

}


object ChapterTable : Table<Chapter, Uuid>("chapters"), VersionedWithTimestamp {

    val Id by idCol(Chapter.BasicInfo::id, Chapter::basicInfo)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, Chapter::basicInfo, name = "courseId")
    val UrlSegment by urlSegmentCol(Chapter.BasicInfo::urlSegment, Chapter::basicInfo)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText, Chapter::basicInfo)
    val MetaTitle by metaTitleCol(Chapter::meta)
    val MetaDescription by metaDescriptionCol(Chapter::meta)
    val MetaKeywords by metaKeywordsCol(Chapter::meta)
    val Heading by headingCol(Chapter::heading)
    val Description by col(Chapter::description, name = "description")
    val SortIndex by sortIndexCol(Chapter::sortIndex)
    val LastModified by lastModifiedCol(Chapter::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Chapter, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Chapter>): Chapter = Chapter(
            basicInfo = Chapter.BasicInfo(
                    id = value of Id,
                    courseId = value of CourseId,
                    urlSegment = value of UrlSegment,
                    linkText = value of LinkText
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            heading = value of Heading,
            description = value of Description,
            sortIndex = value of SortIndex,
            lastModified = value of LastModified
    )

}


object BasicChapterInfoTable : Table<Chapter.BasicInfo, Uuid>("chapters") {

    val Id by idCol(Chapter.BasicInfo::id)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, name = "courseId")
    val UrlSegment by urlSegmentCol(Chapter.BasicInfo::urlSegment)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Chapter.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Chapter.BasicInfo>): Chapter.BasicInfo = Chapter.BasicInfo(
            id = value of Id,
            courseId = value of CourseId,
            urlSegment = value of UrlSegment,
            linkText = value of LinkText
    )

}


class ChapterDao(session: Session) : AbstractDao<Chapter, Uuid>(session, ChapterTable, ChapterTable.Id.property) {

    private val tableName = ChapterTable.name
    private val idColName = ChapterTable.Id.name
    private val courseIdColName = ChapterTable.CourseId.name
    private val urlSegmentColName = ChapterTable.UrlSegment.name
    private val sortIndexColName = ChapterTable.SortIndex.name

    private val basicColumns = """id, "courseId", "urlSegment", "linkText""""

    override val defaultOrder: Map<Column<Chapter, *>, OrderByDirection> =
            mapOf(ChapterTable.SortIndex to OrderByDirection.ASC)


    fun findAllBasicSorted(courseId: Uuid): List<Chapter.BasicInfo> =
            session.select(
                    sql = """SELECT $basicColumns FROM $tableName WHERE "$courseIdColName" = :courseId ORDER BY "$sortIndexColName"""",
                    parameters = mapOf("courseId" to courseId),
                    mapper = BasicChapterInfoTable.rowMapper()
            )

    fun findBasicById(chapterId: Uuid): Chapter.BasicInfo? =
            session.select(
                    sql = """SELECT $basicColumns FROM $tableName WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to chapterId),
                    mapper = BasicChapterInfoTable.rowMapper()
            ).singleOrNull()

    fun findById(chapterId: Uuid): Chapter? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to chapterId),
                    mapper = ChapterTable.rowMapper()
            ).singleOrNull()

    fun findByUrlSegment(courseId: Uuid, segment: String): Chapter? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$courseIdColName" = :cId AND "$urlSegmentColName" = :segment LIMIT 1""",
                    parameters = hashMapOf("cId" to courseId, "segment" to segment),
                    mapper = ChapterTable.rowMapper()
            ).singleOrNull()

    fun findBasicByUrlSegment(courseId: Uuid, segment: String): Chapter.BasicInfo? =
            session.select(
                    sql = """SELECT $basicColumns FROM $tableName WHERE "$courseIdColName" = :cId AND "$urlSegmentColName" = :segment LIMIT 1""",
                    parameters = hashMapOf("cId" to courseId, "segment" to segment),
                    mapper = BasicChapterInfoTable.rowMapper()
            ).singleOrNull()

    fun findPreviousAndNextBasic(chapter: Chapter): Pair<Chapter.BasicInfo?, Chapter.BasicInfo?> {
        val params = hashMapOf("cId" to chapter.basicInfo.courseId, "idx" to chapter.sortIndex)
        val mapper = BasicChapterInfoTable.rowMapper()

        return session.select(
                """SELECT $basicColumns FROM $tableName WHERE "$courseIdColName" = :cId AND "$sortIndexColName" < :idx ORDER BY "$sortIndexColName" DESC LIMIT 1""",
                parameters = params, mapper = mapper
        ).singleOrNull() to session.select(
                """SELECT $basicColumns FROM $tableName WHERE "$courseIdColName" = :cId AND "$sortIndexColName" > :idx ORDER BY "$sortIndexColName" ASC LIMIT 1""",
                parameters = params, mapper = mapper
        ).singleOrNull()
    }

}


/*
CREATE TABLE public.chapters (
	"id" uuid NOT NULL,
	"courseId" uuid NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"heading" varchar(256) NOT NULL,
	"description" text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT chapters_pk PRIMARY KEY (id),
	CONSTRAINT chapters_courses_fk FOREIGN KEY ("courseId") REFERENCES public.courses(id)
)
WITH (
	OIDS=FALSE
) ;
CREATE INDEX chapters_urlsegment_idx ON public.chapters ("urlSegment") ;
 */
