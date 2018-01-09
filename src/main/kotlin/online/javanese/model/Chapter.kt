package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime

class Chapter(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val h1: String,
        val description: Html,
        val sortIndex: Int,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val courseId: Uuid,
            val urlPathComponent: String,
            val linkText: String
    )

}

class ChapterTree internal constructor(
        val id: Uuid,
        val courseId: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        val course: CourseTree,
        lessons: (ChapterTree) -> List<LessonTree>
) {
    val lessons = lessons(this)
}

object ChapterTable : Table<Chapter, Uuid>("chapters"), VersionedWithTimestamp {

    val Id by idCol(Chapter.BasicInfo::id, Chapter::basicInfo)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, Chapter::basicInfo, name = "courseId")
    val UrlPathComponent by urlSegmentCol(Chapter.BasicInfo::urlPathComponent, Chapter::basicInfo)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText, Chapter::basicInfo)
    val MetaTitle by metaTitleCol(Chapter::meta)
    val MetaDescription by metaDescriptionCol(Chapter::meta)
    val MetaKeywords by metaKeywordsCol(Chapter::meta)
    val H1 by headingCol(Chapter::h1)
    val Description by col(Chapter::description, name = "description")
    val SortIndex by sortIndexCol(Chapter::sortIndex)
    val LastModified by lastModifiedCol(Chapter::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Chapter, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Chapter>): Chapter = Chapter(
            basicInfo = Chapter.BasicInfo(
                    id = value of Id,
                    courseId = value of CourseId,
                    urlPathComponent = value of UrlPathComponent,
                    linkText = value of LinkText
            ),
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            h1 = value of H1,
            description = value of Description,
            sortIndex = value of SortIndex,
            lastModified = value of LastModified
    )

}

object BasicChapterInfoTable : Table<Chapter.BasicInfo, Uuid>("chapters") {

    val Id by idCol(Chapter.BasicInfo::id)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, name = "courseId")
    val UrlPathComponent by urlSegmentCol(Chapter.BasicInfo::urlPathComponent)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Chapter.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Chapter.BasicInfo>): Chapter.BasicInfo = Chapter.BasicInfo(
            id = value of Id,
            courseId = value of CourseId,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

class ChapterDao(
        session: Session,
        private val lessonDao: LessonDao
) : AbstractDao<Chapter, Uuid>(session, ChapterTable, ChapterTable.Id.property) {

    private val tableName = ChapterTable.name
    private val idColName = ChapterTable.Id.name
    private val courseIdColName = ChapterTable.CourseId.name
    private val urlComponentColName = ChapterTable.UrlPathComponent.name
    private val sortIndexColName = ChapterTable.SortIndex.name

    private val basicColumns = """id, "courseId", "urlSegment", "linkText""""

    override val defaultOrder: Map<Column<Chapter, *>, OrderByDirection> =
            mapOf(ChapterTable.SortIndex to OrderByDirection.ASC)

    fun findBasicSortedBySortIndex(courseId: Uuid): List<Chapter.BasicInfo> =
            session.select(
                    sql = """SELECT $basicColumns FROM $tableName WHERE "$courseIdColName" = :courseId ORDER BY "$sortIndexColName"""",
                    parameters = mapOf("courseId" to courseId),
                    mapper = BasicChapterInfoTable.rowMapper()
            )

    fun findTreeSortedBySortIndex(course: CourseTree): List<ChapterTree> =
            findBasicSortedBySortIndex(course.id).map { it.toTree(course) }

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

    fun findByUrlComponent(component: String): Chapter? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlComponentColName" = :component LIMIT 1""",
                    parameters = mapOf("component" to component),
                    mapper = ChapterTable.rowMapper()
            ).singleOrNull()

    private fun Chapter.BasicInfo.toTree(course: CourseTree) = ChapterTree(
            id = id,
            courseId = courseId,
            urlPathComponent = urlPathComponent,
            linkText = linkText,
            course = course,
            lessons = lessonDao::findTreeSortedBySortIndex
    )

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
 */
