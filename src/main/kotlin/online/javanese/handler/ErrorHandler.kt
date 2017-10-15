package online.javanese.handler

import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText

fun ErrorHandler(
        errorPage: (Int, String) -> String
): suspend (ApplicationCall) -> Unit = { call ->
    val status = call.response.status()!!
    call.respondText(errorPage(status.value, status.description), ContentType.Text.Html)
}
