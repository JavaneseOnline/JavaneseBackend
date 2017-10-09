package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.*
import online.javanese.Html
import online.javanese.Uuid

internal class Task(
        val basicInfo: BasicInfo,
        val heading: String,
        val condition: Html,
        val technicalInfo: TechnicalInfo,
        val sortIndex: Int
) {

    class BasicInfo(
            val id: Uuid,
            val lessonId: Uuid,
            val linkText: String,
            val urlPathComponent: String
    )

    class TechnicalInfo(
            val compiledClassName: String,
            val inputMethod: InputMethod,
            val initialCode: String,
            val codeToAppend: String,
            val allowSystemIn: Boolean,
            val checkRules: String,
            val expectedOutput: String,
            val timeLimit: Int, // seconds
            val memoryLimit: Int // MB
    )

    enum class InputMethod {
        MethodBody, // user inputs method body
        ClassBody,  // ... class body
        WholeClass  // ... whole class code, including imports
    }

}

private object TaskTable : Table<Task, Uuid>("tasks") {

    val Id by idCol(Task.BasicInfo::id, Task::basicInfo)
    val LessonId by uuidCol(Task.BasicInfo::lessonId, Task::basicInfo, name = "lessonId")
    val LinkText by linkTextCol(Task.BasicInfo::linkText, Task::basicInfo)
    val UrlPathComponent by urlPathComponentCol(Task.BasicInfo::urlPathComponent, Task::basicInfo)

    val Heading by col(Task::heading, name = "heading")
    val Condition by col(Task::condition, name = "condition")

    val CompiledClassName by col(Task.TechnicalInfo::compiledClassName, Task::technicalInfo,
            name = "compiledClassName")
    val InputMethod by col(Task.TechnicalInfo::inputMethod, Task::technicalInfo,
            name = "inputMethod", default = Task.InputMethod.MethodBody)
    val InitialCode by col(Task.TechnicalInfo::initialCode, Task::technicalInfo,
            name = "initialCode")
    val CodeToAppend by col(Task.TechnicalInfo::codeToAppend, Task::technicalInfo,
            name = "codeToAppend")
    val AllowSystemIn by col(Task.TechnicalInfo::allowSystemIn, Task::technicalInfo,
            name = "allowSystemIn")
    val CheckRequirements by col(Task.TechnicalInfo::checkRules, Task::technicalInfo,
            name = "checkRules")
    val ExpectedOutput by col(Task.TechnicalInfo::expectedOutput, Task::technicalInfo,
            name = "expectedOutput")
    val TimeLimit by col(Task.TechnicalInfo::timeLimit, Task::technicalInfo,
            name = "timeLimit")
    val MemoryLimit by col(Task.TechnicalInfo::memoryLimit, Task::technicalInfo,
            name = "memoryLimit")

    val SortIndex by sortIndexCol(Task::sortIndex)


    override fun idColumns(id: Uuid): Set<Pair<Column<Task, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Task>): Task = Task(
            basicInfo = Task.BasicInfo(
                    id = value of Id,
                    lessonId = value of LessonId,
                    linkText = value of LinkText,
                    urlPathComponent = value of UrlPathComponent
            ),
            heading = value of Heading,
            condition = value of Condition,
            technicalInfo = Task.TechnicalInfo(
                    compiledClassName = value of CompiledClassName,
                    inputMethod = value of InputMethod,
                    initialCode = value of InitialCode,
                    codeToAppend = value of CodeToAppend,
                    allowSystemIn = value of AllowSystemIn,
                    checkRules = value of CheckRequirements,
                    expectedOutput = value of ExpectedOutput,
                    timeLimit = value of TimeLimit,
                    memoryLimit = value of MemoryLimit
            ),
            sortIndex = value of SortIndex
    )

}

private object TaskBasicInfoTable : Table<Task.BasicInfo, Uuid>("tasks") {

    val Id by idCol(Task.BasicInfo::id)
    val LessonId by uuidCol(Task.BasicInfo::lessonId, name = "lessonId")
    val LinkText by linkTextCol(Task.BasicInfo::linkText)
    val UrlPathComponent by urlPathComponentCol(Task.BasicInfo::urlPathComponent)

    override fun idColumns(id: Uuid): Set<Pair<Column<Task.BasicInfo, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<Task.BasicInfo>): Task.BasicInfo = Task.BasicInfo(
            id = value of Id,
            lessonId = value of LessonId,
            linkText = value of LinkText,
            urlPathComponent = value of UrlPathComponent
    )

}

internal class TaskDao(
        private val session: Session,
        private val dao: Dao<Task, Uuid> = object : AbstractDao<Task, Uuid>(session, TaskTable, { it.basicInfo.id }) {}
) {

    fun findBasicSortedBySortIndex(lessonId: Uuid) =
            session.select(
                    sql = """SELECT "id", "lessonId", "linkText", "urlPathComponent" FROM tasks WHERE "lessonId" = :lessonId""",
                    parameters = mapOf("lessonId" to lessonId),
                    mapper = TaskBasicInfoTable.rowMapper()
            )

}

/*
CREATE TABLE public.tasks (
	id uuid NOT NULL,
	"lessonId" uuid NOT NULL,
	"linkText" varchar(256) NOT NULL,
	"urlPathComponent" varchar(64) NOT NULL,
	heading varchar(256) NOT NULL,
	"condition" text NOT NULL,
	"compiledClassName" varchar(64) NOT NULL,
	"inputMethod" varchar(64) NOT NULL,
	"initialCode" text NOT NULL,
	"codeToAppend" text NOT NULL,
	"allowSystemIn" bool NOT NULL,
	"checkRules" text NOT NULL,
	"expectedOutput" text NOT NULL,
	"timeLimit" int4 NOT NULL,
	"memoryLimit" int4 NOT NULL,
	"sortIndex" int4 NOT NULL,
	CONSTRAINT tasks_pk PRIMARY KEY (id),
	CONSTRAINT tasks_lessons_fk FOREIGN KEY ("lessonId") REFERENCES public.lessons(id)
)
WITH (
	OIDS=FALSE
) ;
 */
