package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.Uuid
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

private object LessonTable : Table<Lesson, Uuid>("lessons") {

    val Id by idCol(Lesson.BasicInfo::id, Lesson::basicInfo)
    val ChapterId by uuidCol(Lesson.BasicInfo::chapterId, Lesson::basicInfo, name = "chapterId")
    val UrlPathComponent by urlPathComponentCol(Lesson.BasicInfo::urlPathComponent, Lesson::basicInfo)
    val LinkText by col(Lesson.BasicInfo::linkText, Lesson::basicInfo, name = "linkText")
    val MetaTitle by metaTitleCol(Lesson::meta)
    val MetaDescription by metaDescriptionCol(Lesson::meta)
    val MetaKeywords by metaKeywordsCol(Lesson::meta)
    val H1 by col(Lesson::h1, name = "h1")
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

private object BasicLessonInfoTable : Table<Lesson.BasicInfo, Uuid>("lessons") {

    val Id by idCol(Lesson.BasicInfo::id)
    val ChapterId by uuidCol(Lesson.BasicInfo::chapterId, name = "chapterId")
    val UrlPathComponent by urlPathComponentCol(Lesson.BasicInfo::urlPathComponent)
    val LinkText by col(Lesson.BasicInfo::linkText, name = "linkText")

    override fun idColumns(id: Uuid): Set<Pair<Column<Lesson.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Lesson.BasicInfo>): Lesson.BasicInfo = Lesson.BasicInfo(
            id = value of Id,
            chapterId = value of ChapterId,
            urlPathComponent = value of UrlPathComponent,
            linkText = value of LinkText
    )

}

internal class LessonDao(
        private val session: Session,
        private val baseDao: Dao<Lesson, Uuid> = object : AbstractDao<Lesson, Uuid>(session, LessonTable, { it.basicInfo.id }) {}
) {

    private val tableName = LessonTable.name

    internal fun findTreeSortedBySortIndex(chapterId: Uuid): List<Lesson.BasicInfo> =
            session.select(
                    sql = """SELECT "id", "chapterId", "urlPathComponent", "linkText" FROM "$tableName" WHERE "chapterId" = :chapterId ORDER BY "sortIndex"""",
                    parameters = mapOf("chapterId" to chapterId),
                    mapper = BasicLessonInfoTable.rowMapper()
            )

}

/*
CREATE TABLE public.lessons (
	id uuid NOT NULL,
	"chapterId" uuid NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"metaTitle" varchar(256) NOT NULL,
	"metaDescription" varchar(256) NOT NULL,
	"metaKeywords" varchar(256) NOT NULL,
	h1 varchar(256) NOT NULL,
	"bodyMarkup" text NOT NULL,
	"sortIndex" int4 NOT NULL,
	"lastModified" timestamp NOT NULL,
	CONSTRAINT lessons_pk PRIMARY KEY (id),
	CONSTRAINT lessons_chapters_fk FOREIGN KEY (chapterId) REFERENCES public.chapters(id)
)
WITH (
	OIDS=FALSE
) ;
 */
