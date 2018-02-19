package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.krud.kwery.Uuid
import java.time.LocalDateTime


class Course(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val heading: String,
        val description: Html,
        val sortIndex: Int,
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val urlSegment: String,
            val linkText: String,
            val subtitle: String,
            val icon: String
    )

}

object CourseTable : Table<Course, Uuid>("courses"), VersionedWithTimestamp {

    val Id by idCol(Course.BasicInfo::id, Course::basicInfo)
    val UrlSegment by urlSegmentCol(Course.BasicInfo::urlSegment, Course::basicInfo)
    val MetaTitle by metaTitleCol(Course::meta)
    val MetaDescription by metaDescriptionCol(Course::meta)
    val MetaKeywords by metaKeywordsCol(Course::meta)
    val LinkText by linkTextCol(Course.BasicInfo::linkText, Course::basicInfo)
    val Subtitle by col(Course.BasicInfo::subtitle, Course::basicInfo, name = "subtitle")
    val Icon by col(Course.BasicInfo::icon, Course::basicInfo, name = "icon")
    val Heading by headingCol(Course::heading)
    val Description by col(Course::description, name = "description")
    val SortIndex by sortIndexCol(Course::sortIndex)
    val LastModified by lastModifiedCol(Course::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Course, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course>): Course = Course(
            basicInfo = Course.BasicInfo(
                    id = value of Id,
                    urlSegment = value of UrlSegment,
                    linkText = value of LinkText,
                    subtitle = value of Subtitle,
                    icon = value of Icon
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


object BasicCourseInfoTable : Table<Course.BasicInfo, Uuid>("courses") {

    val Id by idCol(Course.BasicInfo::id)
    val UrlSegment by urlSegmentCol(Course.BasicInfo::urlSegment)
    val LinkText by linkTextCol(Course.BasicInfo::linkText)
    val Subtitle by col(Course.BasicInfo::subtitle, name = "subtitle")
    val Icon by col(Course.BasicInfo::icon, name = "icon")

    override fun idColumns(id: Uuid): Set<Pair<Column<Course.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course.BasicInfo>): Course.BasicInfo = Course.BasicInfo(
            id = value of Id,
            urlSegment = value of UrlSegment,
            linkText = value of LinkText,
            subtitle = value of Subtitle,
            icon = value of Icon
    )

}


class CourseDao(session: Session) : AbstractDao<Course, Uuid>(session, CourseTable, CourseTable.Id.property) {

    private val tableName = CourseTable.name

    private val idColName = CourseTable.Id.name
    private val sortIndexColName = CourseTable.SortIndex.name
    private val urlSegmentColName = CourseTable.UrlSegment.name

    private val basicInfoColumns = """ "id", "urlSegment", "linkText", "subtitle", "icon" """

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

    fun findByUrlSegment(segment: String): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlSegmentColName" = :segment LIMIT 1""",
                    parameters = mapOf("segment" to segment),
                    mapper = CourseTable.rowMapper()
            ).singleOrNull()

    fun findBasicByUrlSegment(segment: String): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$urlSegmentColName" = :segment LIMIT 1""",
                    parameters = mapOf("segment" to segment),
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
	"subtitle" varchar(64) NOT NULL,
	"icon" varchar(64) NOT NULL,
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
CREATE UNIQUE INDEX courses_urlsegment_idx ON public.courses ("urlSegment") ;
 */
