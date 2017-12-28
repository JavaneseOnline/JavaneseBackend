package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.request.receiveParameters
import io.ktor.response.respondText
import online.javanese.krud.kwery.kweryEntityMapping.MapToKweryEntityMapper
import online.javanese.krud.toStringMap
import online.javanese.model.TaskErrorReportDao
import online.javanese.model.TaskErrorReportTable


fun SubmitTaskErrorReportHandler(
        taskErrorReportDao: TaskErrorReportDao
): suspend (ApplicationCall) -> Unit {

    val valuesToTaskErrorReport =
            MapToKweryEntityMapper(TaskErrorReportTable)

    // todo: validate input data

    return { call ->
        val valuesMap = call.receiveParameters()
        val report = valuesToTaskErrorReport(valuesMap.toStringMap())
        taskErrorReportDao.insert(report)
        call.respondText("ok", ContentType.Text.Plain)
    }
}
