package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Uuid
import java.time.LocalDateTime

class Course(
        val id: Uuid,
        val urlPathComponent: String,
        val meta: Meta,
        val title: String,
        val description: String,
        val sortIndex: Int, // todo: use this index in admin panel
        val lastModified: LocalDateTime
        // todo: chapters
)

object CoursesTable : Table<Course, Uuid>("courses"), VersionedWithTimestamp {

    val Id by uuidCol(Course::id)
    val UrlPathComponent by urlPathComponentCol(Course::urlPathComponent)
    val MetaTitle by metaTitleCol(Course::meta)
    val MetaDescription by metaDescriptionCol(Course::meta)
    val MetaKeywords by metaKeywordsCol(Course::meta)
    val Title by col(Course::title, name = "title")
    val Description by col(Course::description, name = "description")
    val SortIndex by col(Course::sortIndex, name = "sortIndex")
    val LastModified by lastModifiedCol(Course::lastModified)

    override fun idColumns(id: Uuid): Set<Pair<Column<Course, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Course>): Course = Course(
            id = value of Id,
            urlPathComponent = value of UrlPathComponent,
            meta = Meta(
                    title = value of MetaTitle,
                    description = value of MetaDescription,
                    keywords = value of MetaKeywords
            ),
            title = value of Title,
            description = value of Description,
            sortIndex = value of SortIndex,
            lastModified = value of LastModified
    )

}

class CourseDao(
        private val session: Session,
        private val baseDao: Dao<Course, Uuid> = object : AbstractDao<Course, Uuid>(session, CoursesTable, Course::id) {}
): Dao<Course, Uuid> by baseDao {

    private val tableName = CoursesTable.name
    private val urlPathComponentName = CoursesTable.UrlPathComponent.name

    fun findAllSortedBySortIndex() =
            session.select(
                    sql = """SELECT * FROM $tableName ORDER BY "$urlPathComponentName" ASC""",
                    mapper = CoursesTable.rowMapper()
            )

}

/*
CREATE TABLE public.courses (
	id uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	title varchar(256) NOT NULL,
	description text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT courses_pk PRIMARY KEY (id)
)
WITH (
	OIDS=FALSE
) ;
 */
