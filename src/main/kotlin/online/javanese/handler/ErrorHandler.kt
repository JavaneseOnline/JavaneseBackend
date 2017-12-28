package online.javanese.handler

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText


fun ErrorHandler(
        errorPage: (Int, String) -> String
): suspend (ApplicationCall) -> Unit = { call ->
    val status = call.response.status()!!
    call.respondText(errorPage(status.value, status.description), ContentType.Text.Html)
}
