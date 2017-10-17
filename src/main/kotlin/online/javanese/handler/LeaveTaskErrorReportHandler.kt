package online.javanese.handler

import online.javanese.ValuesMapToKweryEntityMapper
import online.javanese.model.TaskErrorReportDao
import online.javanese.model.TaskErrorReportTable
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.request.receiveParameters
import org.jetbrains.ktor.response.respondText

fun LeaveTaskErrorReportHandler(
        taskErrorReportDao: TaskErrorReportDao
): suspend (ApplicationCall) -> Unit {

    val valuesMapToTaskErrorReport =
            ValuesMapToKweryEntityMapper(TaskErrorReportTable)

    // todo: validate input data

    return { call ->
        val valuesMap = call.receiveParameters()
        val report = valuesMapToTaskErrorReport(valuesMap)
        taskErrorReportDao.insert(report)
        call.respondText("ok", ContentType.Text.Plain)
    }
}
