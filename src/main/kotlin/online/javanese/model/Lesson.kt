package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime

class Lesson(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val h1: String,
        val bodyMarkup: Html,
        val sortIndex: Int,
        val lastModified: LocalDateTime

) {

    class BasicInfo(
            val id: Uuid,
            val chapterId: Uuid,
            val urlPathComponent: String,
            val linkText: String
    )

}

object LessonTable : Table<Lesson, Uuid>("lessons"), VersionedWithTimestamp {

    val Id by idCol(Lesson.BasicInfo::id, Lesson::basicInfo)
    val ChapterId by uuidCol(Lesson.BasicInfo::chapterId, Lesson::basicInfo, name = "chapterId")
    val UrlPathComponent by urlSegmentCol(Lesson.BasicInfo::urlPathComponent, Lesson::basicInfo)
    val LinkText by linkTextCol(Lesson.BasicInfo::linkText, Lesson::basicInfo)
    val MetaTitle by metaTitleCol(Lesson::meta)
    val MetaDescription by metaDescriptionCol(Lesson::meta)
    val MetaKeywords by metaKeywordsCol(Lesson::meta)
    val H1 by headingCol(Lesson::h1)
    val BodyMarkup by col(Lesson::bodyMarkup, name = "bodyMarkup")
    val SortIndex by sortIndexCol(Lesson::sortIndex)
    val LastModified by lastModifiedCol(Lesson::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Lesson, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Lesson>): Lesson = Lesson (
            basicInfo = Lesson.BasicInfo(
                    id = value of LessonTable.Id,
                    chapterId = value of LessonTable.ChapterId,
                    urlPathComponent = value of LessonTable.UrlPathComponent,
                    linkText = value of LessonTable.LinkText
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    keywords = value of MetaKeywords,
                    description = value of MetaDescription
            ),
            h1 = value of H1,
            bodyMarkup = value of BodyMarkup,
            sortIndex = value of SortIndex,
            lastModified = value of LastModified
    )

}

object BasicLessonInfoTable : Table<Lesson.BasicInfo, Uuid>("lessons") {

    val Id by idCol(Lesson.BasicInfo::id)
    val ChapterId by uuidCol(Lesson.BasicInfo::chapterId, name = "chapterId")
    val UrlPathComponent by urlSegmentCol(Lesson.BasicInfo::urlPathComponent)
    val LinkText by linkTextCol(Lesson.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Lesson.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Lesson.BasicInfo>): Lesson.BasicInfo = Lesson.BasicInfo(
            id = value of Id,
            chapterId = value of ChapterId,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

class LessonDao(session: Session) : AbstractDao<Lesson, Uuid>(session, LessonTable, { it.basicInfo.id }) {

    private val tableName = LessonTable.name

    private val basicCols = """"id", "chapterId", "urlSegment", "linkText""""
    private val idColName = LessonTable.Id.name
    private val urlSegmColName = LessonTable.UrlPathComponent.name
    private val sortIndexColName = LessonTable.SortIndex.name
    private val chapterIdColName = LessonTable.ChapterId.name

    override val defaultOrder: Map<Column<Lesson, *>, OrderByDirection> =
            mapOf(LessonTable.SortIndex to OrderByDirection.ASC)

    internal fun findAllBasicSorted(chapterId: Uuid): List<Lesson.BasicInfo> =
            session.select(
                    sql = """SELECT $basicCols FROM "$tableName" WHERE "chapterId" = :chapterId ORDER BY "sortIndex"""",
                    parameters = mapOf("chapterId" to chapterId),
                    mapper = BasicLessonInfoTable.rowMapper()
            )

    internal fun findBasicById(id: Uuid): Lesson.BasicInfo? =
            session.select(
                    sql = """SELECT $basicCols FROM "$tableName" WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to id),
                    mapper = BasicLessonInfoTable.rowMapper()
            ).singleOrNull()

    fun findByUrlSegment(segment: String): Lesson? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlSegmColName" = :segm LIMIT 1""",
                    parameters = mapOf("segm" to segment),
                    mapper = LessonTable.rowMapper()
            ).singleOrNull()

    internal fun findById(id: Uuid): Lesson? =
            session.select(
                    sql = """SELECT * FROM "$tableName" WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to id),
                    mapper = LessonTable.rowMapper()
            ).singleOrNull()

    fun findPreviousAndNext(lesson: Lesson): Pair<Lesson.BasicInfo?, Lesson.BasicInfo?> {
        val params = mapOf("cId" to lesson.basicInfo.chapterId, "idx" to lesson.sortIndex)
        val mapper = BasicLessonInfoTable.rowMapper()

        return session.select(
                sql = """SELECT $basicCols FROM $tableName WHERE "$chapterIdColName" = :cId AND "$sortIndexColName" < :idx ORDER BY "$sortIndexColName" DESC LIMIT 1""",
                parameters = params, mapper = mapper
        ).singleOrNull() to session.select(
                sql = """SELECT $basicCols FROM $tableName WHERE "$chapterIdColName" = :cId AND "$sortIndexColName" > :idx ORDER BY "$sortIndexColName" ASC LIMIT 1""",
                parameters = params, mapper = mapper
        ).singleOrNull()
    }

}


/*
CREATE TABLE public.lessons (
	"id" uuid NOT NULL,
	"chapterId" uuid NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"heading" varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT lessons_pk PRIMARY KEY (id),
	CONSTRAINT lessons_chapters_fk FOREIGN KEY (chapterId) REFERENCES public.chapters(id)
)
WITH (
	OIDS=FALSE
) ;
CREATE INDEX lessons_urlsegment_idx ON public.lessons ("urlSegment") ;
 */
