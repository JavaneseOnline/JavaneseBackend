package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class Lesson(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val heading: String,
        val bodyMarkup: Html,
        val programmingLanguages: Set<Language>,
        val sortIndex: Int,
        val lastModified: LocalDateTime

) {

    class BasicInfo(
            val id: Uuid,
            val chapterId: Uuid,
            val urlSegment: String,
            val linkText: String
    )

    enum class Language {
        Java, Kotlin
    }

}


private val _path = Lesson::basicInfo

private val _id = Lesson.BasicInfo::id
private val _chapterId = Lesson.BasicInfo::chapterId
private val _urlSegment = Lesson.BasicInfo::urlSegment
private val _linkText = Lesson.BasicInfo::linkText

private val _meta = Lesson::meta


object LessonTable : Table<Lesson, Uuid>("lessons"), VersionedWithTimestamp {

    val Id by idCol(_id, _path)
    val ChapterId by uuidCol(_chapterId, _path, name = "chapterId")
    val UrlSegment by urlSegmentCol(_urlSegment, _path)
    val LinkText by linkTextCol(_linkText, _path)
    val MetaTitle by metaTitleCol(_meta)
    val MetaDescription by metaDescriptionCol(_meta)
    val MetaKeywords by metaKeywordsCol(_meta)
    val Heading by headingCol(Lesson::heading)
    val BodyMarkup by col(Lesson::bodyMarkup, name = "bodyMarkup")
    val ProgrammingLanguages by col(Lesson::programmingLanguages, name = "programmingLanguages", default = emptySet())
    val SortIndex by sortIndexCol(Lesson::sortIndex)
    val LastModified by lastModifiedCol(Lesson::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Lesson, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Lesson>): Lesson = Lesson (
            basicInfo = Lesson.BasicInfo(
                    id = value of LessonTable.Id,
                    chapterId = value of LessonTable.ChapterId,
                    urlSegment = value of LessonTable.UrlSegment,
                    linkText = value of LessonTable.LinkText
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    keywords = value of MetaKeywords,
                    description = value of MetaDescription
            ),
            heading = value of Heading,
            bodyMarkup = value of BodyMarkup,
            programmingLanguages = value of ProgrammingLanguages,
            sortIndex = value of SortIndex,
            lastModified = value of LastModified
    )

}

object BasicLessonInfoTable : Table<Lesson.BasicInfo, Uuid>("lessons") {

    val Id by idCol(_id)
    val ChapterId by uuidCol(_chapterId, name = "chapterId")
    val UrlSegment by urlSegmentCol(_urlSegment)
    val LinkText by linkTextCol(_linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Lesson.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Lesson.BasicInfo>): Lesson.BasicInfo = Lesson.BasicInfo(
            id = value of Id,
            chapterId = value of ChapterId,
            urlSegment = value of UrlSegment,
            linkText = value of LinkText
    )

}

class LessonDao(session: Session) : AbstractDao<Lesson, Uuid>(session, LessonTable, { it.basicInfo.id }) {

    private val tableName = LessonTable.name

    private val basicCols = """"id", "chapterId", "urlSegment", "linkText""""
    private val idColName = LessonTable.Id.name
    private val urlSegmColName = LessonTable.UrlSegment.name
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

    fun findByUrlSegment(chapterId: Uuid, segment: String): Lesson? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$chapterIdColName" = :cId AND "$urlSegmColName" = :segment LIMIT 1""",
                    parameters = hashMapOf("cId" to chapterId, "segment" to segment),
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
	"programmingLanguages" varchar(64) NOT NULL,
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
