package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import online.javanese.krud.kwery.kweryEntityMapping.MapToKweryEntityMapper
import online.javanese.model.TaskErrorReportDao
import online.javanese.model.TaskErrorReportTable


fun SubmitTaskErrorReportHandler(
        taskErrorReportDao: TaskErrorReportDao
): suspend (ApplicationCall, Unit) -> Unit {

    val valuesToTaskErrorReport =
            MapToKweryEntityMapper(TaskErrorReportTable)

    // todo: validate input data

    return { call, _ ->
        val valuesMap = call.receiveParameters()
        val report = valuesToTaskErrorReport(valuesMap)
        taskErrorReportDao.insert(report)
        call.respond(HttpStatusCode.NoContent, "")
    }
}
