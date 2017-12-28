package online.javanese.model

import com.github.andrewoma.kwery.core.Session
import com.github.andrewoma.kwery.mapper.AbstractDao
import com.github.andrewoma.kwery.mapper.Column
import com.github.andrewoma.kwery.mapper.Table
import com.github.andrewoma.kwery.mapper.Value
import online.javanese.krud.kwery.Uuid

class TaskErrorReport(
        val id: Uuid,
        val taskId: Uuid,
        val errorKind: ErrorKind,
        val code: String,
        val text: String
) {

    enum class ErrorKind {
        RightSolutionNotAccepted, BadCondition, InsufficientMaterial, Other
    }

}

// ValuesMap to TaskErrorReport mapper requires table
internal object TaskErrorReportTable : Table<TaskErrorReport, Uuid>("taskErrorReports") {

    val Id
            by idCol(TaskErrorReport::id)

    val TaskId
            by uuidCol(TaskErrorReport::taskId, name = "taskId")

    val ErrorKind
            by col(TaskErrorReport::errorKind, name = "errorKind", default = TaskErrorReport.ErrorKind.BadCondition)

    val Code
            by col(TaskErrorReport::code, name = "code")

    val Text
            by col(TaskErrorReport::text, name = "text")


    override fun idColumns(id: Uuid): Set<Pair<Column<TaskErrorReport, *>, *>> =
            setOf(Id of id)

    override fun create(value: Value<TaskErrorReport>): TaskErrorReport = TaskErrorReport(
            id = value of Id,
            taskId = value of TaskId,
            errorKind = value of ErrorKind,
            code = value of Code,
            text = value of Text
    )

}

class TaskErrorReportDao(
        session: Session
) : AbstractDao<TaskErrorReport, Uuid>(session, TaskErrorReportTable, TaskErrorReportTable.Id.property) {

    private val insertSql = insertSql(TaskErrorReportTable)

    fun insert(value: TaskErrorReport): TaskErrorReport =
            insert(table, session, insertSql, value)

}

/*
CREATE TABLE public."taskErrorReports" (
	"id" uuid NOT NULL,
	"taskId" uuid NOT NULL,
	"errorKind" varchar(64) NOT NULL,
	"code" text NOT NULL,
	"text" text NOT NULL,
	CONSTRAINT taskErrorReports_pk PRIMARY KEY (id),
	CONSTRAINT taskErrorReports_tasks_fk FOREIGN KEY ("taskId") REFERENCES public.tasks(id)
)
WITH (
	OIDS=FALSE
) ;
 */
