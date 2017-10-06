package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.Uuid
import java.time.LocalDateTime

class Course(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val h1: String,
        val description: Html, // todo: edit as HTML
        val sortIndex: Int, // todo: use this index in admin panel
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val urlPathComponent: String,
            val linkText: String
    )

}

private object CourseTable : Table<Course, Uuid>("courses"), VersionedWithTimestamp {

    val Id by idCol(Course.BasicInfo::id, Course::basicInfo)
    val UrlPathComponent by urlPathComponentCol(Course.BasicInfo::urlPathComponent, Course::basicInfo)
    val MetaTitle by metaTitleCol(Course::meta)
    val MetaDescription by metaDescriptionCol(Course::meta)
    val MetaKeywords by metaKeywordsCol(Course::meta)
    val LinkText by col(Course.BasicInfo::linkText, Course::basicInfo, name = "linkText")
    val H1 by col(Course::h1, name = "h1")
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

private object BasicCourseInfoTable : Table<Course.BasicInfo, Uuid>("courses") {

    val Id by idCol(Course.BasicInfo::id)
    val UrlPathComponent by urlPathComponentCol(Course.BasicInfo::urlPathComponent)
    val LinkText by col(Course.BasicInfo::linkText, name = "linkText")

    override fun idColumns(id: Uuid): Set<Pair<Column<Course.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course.BasicInfo>): Course.BasicInfo = Course.BasicInfo(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

internal class CourseDao(
        private val session: Session,
        private val baseDao: Dao<Course, Uuid> = object : AbstractDao<Course, Uuid>(session, CourseTable, { it.basicInfo.id }) {}
): Dao<Course, Uuid> by baseDao {

    private val tableName = CourseTable.name
    private val sortIndexColName = CourseTable.SortIndex.name

    override fun findById(id: Uuid, columns: Set<Column<Course, *>>): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "id" = :id""",
                    parameters = mapOf("id" to id),
                    mapper = CourseTable.rowMapper()
            ).firstOrNull()

    fun findAllBasicSortedBySortIndex(): List<Course.BasicInfo> =
            session.select(
                    sql = """SELECT "id", "urlPathComponent", "linkText" FROM $tableName ORDER BY "$sortIndexColName" ASC""",
                    mapper = BasicCourseInfoTable.rowMapper()
            )

    fun findAllSortedBySortIndex() =
            session.select(
                    sql = """SELECT * FROM $tableName ORDER BY "$sortIndexColName" ASC""",
                    mapper = CourseTable.rowMapper()
            )

}

/*
CREATE TABLE public.courses (
	id uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	h1 varchar(256) NOT NULL,
	description text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT courses_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
