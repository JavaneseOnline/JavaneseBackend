package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Table
import com.github.andrewoma.kwery.mapper.Value
import com.github.andrewoma.kwery.mapper.VersionedWithTimestamp
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

class CourseTree internal constructor(
        val id: Uuid,
        val urlPathComponent: String,
        val linkText: String,
        chapters: (CourseTree) -> List<ChapterTree>
) {
    val chapters = chapters(this)
}

private object CourseTable : Table<Course, Uuid>("courses"), VersionedWithTimestamp {

    val Id by idCol(Course.BasicInfo::id, Course::basicInfo)
    val UrlPathComponent by urlPathComponentCol(Course.BasicInfo::urlPathComponent, Course::basicInfo)
    val MetaTitle by metaTitleCol(Course::meta)
    val MetaDescription by metaDescriptionCol(Course::meta)
    val MetaKeywords by metaKeywordsCol(Course::meta)
    val LinkText by linkTextCol(Course.BasicInfo::linkText, Course::basicInfo)
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
    val LinkText by linkTextCol(Course.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Course.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course.BasicInfo>): Course.BasicInfo = Course.BasicInfo(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

class CourseDao(
        private val session: Session,
        private val chapterDao: ChapterDao
) {

//    private val baseDao: Dao<Course, Uuid> = object : AbstractDao<Course, Uuid>(session, CourseTable, { it.basicInfo.id }) {}

    private val tableName = CourseTable.name

    private val idColName = CourseTable.Id.name
    private val sortIndexColName = CourseTable.SortIndex.name
    private val urlComponentColName = CourseTable.UrlPathComponent.name

    private val basicInfoColumns = """ "id", "urlPathComponent", "linkText" """

    /*fun findById(id: Uuid, columns: Set<Column<Course, *>>): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "id" = :id""",
                    parameters = mapOf("id" to id),
                    mapper = CourseTable.rowMapper()
            ).firstOrNull()*/

    fun findAllBasicSortedBySortIndex(): List<Course.BasicInfo> =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName ORDER BY "$sortIndexColName" ASC""",
                    mapper = BasicCourseInfoTable.rowMapper()
            )

    fun findTreeSortedBySortIndex(): List<CourseTree> =
            findAllBasicSortedBySortIndex()
                    .map { toTree(it) }

    fun findBasicById(id: Uuid): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM $tableName WHERE "$idColName" = :id LIMIT 1""",
                    parameters = mapOf("id" to id),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

    fun findTree(courseId: Uuid): CourseTree? =
            findBasicById(courseId)
                    ?.let { toTree(it) }

    /*fun findAllSortedBySortIndex() =
            session.select(
                    sql = """SELECT * FROM $tableName ORDER BY "$sortIndexColName" ASC""",
                    mapper = CourseTable.rowMapper()
            )*/

    fun findByUrlComponent(component: String): Course? =
            session.select(
                    sql = """SELECT * FROM $tableName WHERE "$urlComponentColName" = :component LIMIT 1""",
                    parameters = mapOf("component" to component),
                    mapper = CourseTable.rowMapper()
            ).singleOrNull()

    fun findPrevious(course: Course): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM courses WHERE "$sortIndexColName" < :index ORDER BY "$sortIndexColName" DESC LIMIT 1""",
                    parameters = mapOf("index" to course.sortIndex),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

    fun findNext(course: Course): Course.BasicInfo? =
            session.select(
                    sql = """SELECT $basicInfoColumns FROM courses WHERE "$sortIndexColName" > :index ORDER BY "$sortIndexColName" ASC LIMIT 1""",
                    parameters = mapOf("index" to course.sortIndex),
                    mapper = BasicCourseInfoTable.rowMapper()
            ).singleOrNull()

    private fun toTree(info: Course.BasicInfo): CourseTree = CourseTree(
            id = info.id,
            urlPathComponent = info.urlPathComponent,
            linkText = info.linkText,
            chapters = chapterDao::findTreeSortedBySortIndex
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
