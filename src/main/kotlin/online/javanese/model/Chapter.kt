package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.Uuid
import java.time.LocalDateTime

class Chapter(
        val basicInfo: BasicInfo,
        val meta: Meta,
        val h1: String,
        val description: Html, // todo: edit as HTML
        val sortIndex: Int, // todo: use it
        val lastModified: LocalDateTime
) {

    class BasicInfo(
            val id: Uuid,
            val courseId: Uuid,
            val urlPathComponent: String,
            val linkText: String
    )

}

private object ChapterTable : Table<Chapter, Uuid>("chapters"), VersionedWithTimestamp {

    val Id by idCol(Chapter.BasicInfo::id, Chapter::basicInfo)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, Chapter::basicInfo, name = "courseId")
    val UrlPathComponent by urlPathComponentCol(Chapter.BasicInfo::urlPathComponent, Chapter::basicInfo)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText, Chapter::basicInfo)
    val MetaTitle by metaTitleCol(Chapter::meta)
    val MetaDescription by metaDescriptionCol(Chapter::meta)
    val MetaKeywords by metaKeywordsCol(Chapter::meta)
    val H1 by col(Chapter::h1, name = "h1")
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

private object BasicChapterInfoTable : Table<Chapter.BasicInfo, Uuid>("chapters") {

    val Id by idCol(Chapter.BasicInfo::id)
    val CourseId by uuidCol(Chapter.BasicInfo::courseId, name = "courseId")
    val UrlPathComponent by urlPathComponentCol(Chapter.BasicInfo::urlPathComponent)
    val LinkText by linkTextCol(Chapter.BasicInfo::linkText)

    override fun idColumns(id: Uuid): Set<Pair<Column<Chapter.BasicInfo, *>, *>> = setOf(Id of id)

    override fun create(value: Value<Chapter.BasicInfo>): Chapter.BasicInfo = Chapter.BasicInfo(
            id = value of Id,
            courseId = value of CourseId,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

internal class ChapterDao(
        private val session: Session,
        private val baseDao: Dao<Chapter, Uuid> = object : AbstractDao<Chapter, Uuid>(session, ChapterTable, { it.basicInfo.id }) {}
) : Dao<Chapter, Uuid> by baseDao {

    private val tableName = ChapterTable.name
    private val courseIdColName = ChapterTable.CourseId.name
    private val sortIndexColName = ChapterTable.SortIndex.name

    fun findBasicSortedBySortIndex(courseId: Uuid): List<Chapter.BasicInfo> =
            session.select(
                    sql = """SELECT id, "courseId", "urlPathComponent", "linkText" FROM $tableName WHERE "$courseIdColName" = :courseId ORDER BY "$sortIndexColName"""",
                    parameters = mapOf("courseId" to courseId),
                    mapper = BasicChapterInfoTable.rowMapper()
            )

}

/*
CREATE TABLE public.chapters (
	id uuid NOT NULL,
	"courseId" uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	h1 varchar(256) NOT NULL,
	description text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT chapters_pk PRIMARY KEY (id),
	CONSTRAINT chapters_courses_fk FOREIGN KEY ("courseId") REFERENCES public.courses(id)
)
WITH (
	OIDS=FALSE
) ;
 */
