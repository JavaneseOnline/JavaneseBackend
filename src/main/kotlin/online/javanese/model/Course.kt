package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime

class Course(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val h1: String,
        val description: Html,
        val sortIndex: Int,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val urlPathComponent: String,
            val linkText: String
    )

}

object CourseTable : Table<Course, Uuid>("courses"), VersionedWithTimestamp {

    val Id by idCol(Course.BasicInfo::id, Course::basicInfo)
    val UrlPathComponent by urlSegmentCol(Course.BasicInfo::urlPathComponent, Course::basicInfo)
    val MetaTitle by metaTitleCol(Course::meta)
    val MetaDescription by metaDescriptionCol(Course::meta)
    val MetaKeywords by metaKeywordsCol(Course::meta)
    val LinkText by linkTextCol(Course.BasicInfo::linkText, Course::basicInfo)
    val H1 by headingCol(Course::h1)
    val Description by col(Course::description, name = "description")
    val SortIndex by sortIndexCol(Course::sortIndex)
    val LastModified by lastModifiedCol(Course::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Course, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course>): Course = Course(
            basicInfo = Course.BasicInfo(
                    id = value of Id,
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


object BasicCourseInfoTable : Table<Course.BasicInfo, Uuid>("courses") {

    val Id by idCol(Course.BasicInfo::id)
    val UrlPathComponent by urlSegmentCol(Course.BasicInfo::urlPathComponent)
    val LinkText by linkTextCol(Course.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Course.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course.BasicInfo>): Course.BasicInfo = Course.BasicInfo(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}


class CourseDao(session: Session) : AbstractDao<Course, Uuid>(session, CourseTable, CourseTable.Id.property) {

    private val tableName = CourseTable.name

    private val idColName = CourseTable.Id.name
    private val sortIndexColName = CourseTable.SortIndex.name
    private val urlComponentColName = CourseTable.UrlPathComponent.name

    private val basicInfoColumns = """ "id", "urlSegment", "linkText" """

    override val defaultOrder: Map<Column<Course, *>, OrderByDirection> =
            mapOf(CourseTable.SortIndex to OrderByDirection.ASC)


    fun findAllBasicSorted(): List<Course.BasicInfo> =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName ORDER BY "$sortIndexColName" ASC""",
                    mapper = BasicCourseInfoTable.rowMapper()
            )

    fun findBasicById(id: Uuid): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to id),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

    fun findById(id: Uuid): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to id),
                    mapper = CourseTable.rowMapper()
            ).singleOrNull()

    fun findByUrlComponent(component: String): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlComponentColName" = :component LIMIT 1""",
                    parameters = mapOf("component" to component),
                    mapper = CourseTable.rowMapper()
            ).singleOrNull()

    fun findBasicByUrlComponent(component: String): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$urlComponentColName" = :component LIMIT 1""",
                    parameters = mapOf("component" to component),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

    fun findPreviousAndNextBasic(course: Course): Pair<Course.BasicInfo?, Course.BasicInfo?> =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$sortIndexColName" < :index ORDER BY "$sortIndexColName" DESC LIMIT 1""",
                    parameters = mapOf("index" to course.sortIndex),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull() to session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$sortIndexColName" > :index ORDER BY "$sortIndexColName" ASC LIMIT 1""",
                    parameters = mapOf("index" to course.sortIndex),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

}


/*
CREATE TABLE public.courses (
	"id" uuid NOT NULL,
	"urlSegment" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	"heading" varchar(256) NOT NULL,
	"description" text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT courses_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
